/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.AuthDefinition.AuthType;
import com.palantir.conjure.defs.services.EndpointName;
import com.palantir.conjure.defs.services.ParameterId;
import com.palantir.conjure.defs.services.PathDefinition;
import com.palantir.conjure.defs.services.RequestLineDefinition;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonEndpointDefinition extends Emittable {

    EndpointName methodName();

    RequestLineDefinition http();

    AuthDefinition authDefinition();

    List<PythonEndpointParam> params();

    boolean isBinary();

    Optional<String> pythonReturnType();

    Optional<String> myPyReturnType();

    @Value.Check
    default void check() {
        checkState(pythonReturnType().isPresent() == myPyReturnType().isPresent(),
                "expected both return types or neither");
    }

    @Override
    default void emit(PythonPoetWriter poetWriter) {
        poetWriter.maintainingIndent(() -> {
            // if auth type is header, insert it as a fake param
            List<PythonEndpointParam> paramsWithHeader = authDefinition().type() == AuthType.HEADER
                    ? ImmutableList.<PythonEndpointParam>builder()
                    .add(PythonEndpointParam.builder()
                            .paramName("authHeader")
                            .paramId(ParameterId.of("Authorization"))
                            .myPyType("str")
                            .paramType(ArgumentDefinition.ParamType.HEADER)
                            .build())
                    .addAll(params())
                    .build() : params();
            paramsWithHeader = paramsWithHeader.stream().collect(Collectors.toList());

            poetWriter.writeIndentedLine("def %s(self, %s):",
                    methodName().name(),
                    Joiner.on(", ").join(
                            paramsWithHeader.stream()
                                    .map(PythonEndpointParam::paramName)
                                    .collect(Collectors.toList())));
            poetWriter.increaseIndent();
            poetWriter.writeIndentedLine("# type: (%s) -> %s",
                    Joiner.on(", ").join(
                            paramsWithHeader.stream()
                                    .map(PythonEndpointParam::myPyType)
                                    .collect(Collectors.toList())),
                    myPyReturnType().orElse("None"));

            // header
            poetWriter.writeLine();
            poetWriter.writeIndentedLine("_headers = {");
            poetWriter.increaseIndent();
            poetWriter.writeIndentedLine("'Accept': '%s',",
                    isBinary() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.APPLICATION_JSON);
            poetWriter.writeIndentedLine("'Content-Type': 'application/json',");
            paramsWithHeader.stream()
                    .filter(param -> param.paramType() == ArgumentDefinition.ParamType.HEADER)
                    .forEach(param -> {
                        poetWriter.writeIndentedLine("'%s': %s,",
                                param.paramId().map(ParameterId::name).orElse(param.paramName()), param.paramName());
                    });
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("} # type: Dict[str, Any]");

            // params
            poetWriter.writeLine();
            poetWriter.writeIndentedLine("_params = {");
            poetWriter.increaseIndent();
            paramsWithHeader.stream()
                    .filter(param -> param.paramType() == ArgumentDefinition.ParamType.QUERY)
                    .forEach(param -> {
                        poetWriter.writeIndentedLine("'%s': %s,",
                                param.paramId().map(ParameterId::name).orElse(param.paramName()),
                                param.paramName());
                    });
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("} # type: Dict[str, Any]");

            // path params
            poetWriter.writeLine();
            poetWriter.writeIndentedLine("_path_params = {");
            poetWriter.increaseIndent();
            paramsWithHeader.stream()
                    .filter(param -> param.paramType() == ArgumentDefinition.ParamType.PATH)
                    .forEach(param -> {
                        poetWriter.writeIndentedLine("'%s': %s,",
                                param.paramId().map(ParameterId::name).orElse(param.paramName()),
                                param.paramName());
                    });
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("} # type: Dict[str, Any]");

            // body
            Optional<PythonEndpointParam> bodyParam = paramsWithHeader.stream()
                    .filter(param -> param.paramType() == ArgumentDefinition.ParamType.BODY)
                    .findAny();
            if (bodyParam.isPresent()) {
                poetWriter.writeLine();
                poetWriter.writeIndentedLine("_json = ConjureEncoder().default(%s) # type: Any",
                        bodyParam.get().paramName());
            } else {
                poetWriter.writeLine();
                poetWriter.writeIndentedLine("_json = None # type: Any");
            }

            // fix the path, add path params
            poetWriter.writeLine();

            PathDefinition fullPath = http().path();
            String fixedPath = fullPath.toString().replaceAll("\\{(.*):[^}]*\\}", "{$1}");
            poetWriter.writeIndentedLine("_path = '%s'", fixedPath);
            poetWriter.writeIndentedLine("_path = _path.format(**_path_params)");

            poetWriter.writeLine();
            poetWriter.writeIndentedLine("_response = self._requests_session.request( # type: ignore");
            poetWriter.increaseIndent();
            poetWriter.writeIndentedLine("'%s',", http().method());
            poetWriter.writeIndentedLine("self._uri + _path,");
            poetWriter.writeIndentedLine("params=_params,");
            poetWriter.writeIndentedLine("headers=_headers,");
            if (isBinary()) {
                poetWriter.writeIndentedLine("stream=True,");
            }
            poetWriter.writeIndentedLine("json=_json)");
            poetWriter.decreaseIndent();

            poetWriter.writeLine();
            poetWriter.writeIndentedLine("try:");
            poetWriter.increaseIndent();
            poetWriter.writeIndentedLine("_response.raise_for_status()");
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("except HTTPError as e:");
            poetWriter.increaseIndent();
            poetWriter.writeIndentedLine("detail = e.response.json() if e.response else {}");
            poetWriter.writeIndentedLine("raise HTTPError('{}. Error Name: {}. Message: {}'.format("
                    + "e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), "
                    + "response=_response)");
            poetWriter.decreaseIndent();


            poetWriter.writeLine();
            if (isBinary()) {
                poetWriter.writeIndentedLine("_raw = _response.raw");
                poetWriter.writeIndentedLine("_raw.decode_content = True");
                poetWriter.writeIndentedLine("return _raw");
            } else if (pythonReturnType().isPresent()) {
                poetWriter.writeIndentedLine("_decoder = ConjureDecoder()");
                poetWriter.writeIndentedLine("return _decoder.decode(_response.json(), %s)", pythonReturnType().get());
            } else {
                poetWriter.writeIndentedLine("return");
            }

            poetWriter.decreaseIndent();
        });
    }

    class Builder extends ImmutablePythonEndpointDefinition.Builder {}

    static Builder builder() {
        return new Builder();
    }

    @Value.Immutable
    @ConjureImmutablesStyle
    public interface PythonEndpointParam {

        String paramName();

        /**
         * An identifier to use as a parameter value (e.g. if this is a header parameter, param-id defines the header
         * key); by default the argument name is used as the param-id
         */
        Optional<ParameterId> paramId();

        String myPyType();

        ArgumentDefinition.ParamType paramType();

        class Builder extends ImmutablePythonEndpointParam.Builder {}

        static Builder builder() {
            return new Builder();
        }

    }
}
