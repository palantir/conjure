/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.services.AuthType;
import com.palantir.conjure.defs.services.BodyParameterType;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.EndpointName;
import com.palantir.conjure.defs.services.HeaderAuthType;
import com.palantir.conjure.defs.services.HeaderParameterType;
import com.palantir.conjure.defs.services.HttpPath;
import com.palantir.conjure.defs.services.ParameterType;
import com.palantir.conjure.defs.services.PathParameterType;
import com.palantir.conjure.defs.services.QueryParameterType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonEndpointDefinition extends Emittable {

    EndpointName methodName();

    EndpointDefinition.HttpMethod httpMethod();

    HttpPath httpPath();

    Optional<AuthType> auth();

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
            boolean isHeaderType = auth().isPresent() && (auth().get() instanceof HeaderAuthType);
            List<PythonEndpointParam> paramsWithHeader = isHeaderType
                    ? ImmutableList.<PythonEndpointParam>builder()
                    .add(PythonEndpointParam.builder()
                            .paramName("authHeader")
                            .myPyType("str")
                            .paramType(HeaderParameterType.header("Authorization"))
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
                    .filter(param -> param.paramType() instanceof HeaderParameterType)
                    .forEach(param -> {
                        poetWriter.writeIndentedLine("'%s': %s,",
                                ((HeaderParameterType) param.paramType()).paramId().name(), param.paramName());
                    });
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("} # type: Dict[str, Any]");

            // params
            poetWriter.writeLine();
            poetWriter.writeIndentedLine("_params = {");
            poetWriter.increaseIndent();
            paramsWithHeader.stream()
                    .filter(param -> param.paramType() instanceof QueryParameterType)
                    .forEach(param -> {
                        poetWriter.writeIndentedLine("'%s': %s,",
                                ((QueryParameterType) param.paramType()).paramId().name(),
                                param.paramName());
                    });
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("} # type: Dict[str, Any]");

            // path params
            poetWriter.writeLine();
            poetWriter.writeIndentedLine("_path_params = {");
            poetWriter.increaseIndent();
            // TODO(qchen): no need for param name twice?
            paramsWithHeader.stream()
                    .filter(param -> param.paramType() instanceof PathParameterType)
                    .forEach(param -> {
                        poetWriter.writeIndentedLine("'%s': %s,",
                                param.paramName(),
                                param.paramName());
                    });
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("} # type: Dict[str, Any]");

            // body
            Optional<PythonEndpointParam> bodyParam = paramsWithHeader.stream()
                    .filter(param -> param.paramType() instanceof BodyParameterType)
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

            HttpPath fullPath = httpPath();
            String fixedPath = fullPath.toString().replaceAll("\\{(.*):[^}]*\\}", "{$1}");
            poetWriter.writeIndentedLine("_path = '%s'", fixedPath);
            poetWriter.writeIndentedLine("_path = _path.format(**_path_params)");

            poetWriter.writeLine();
            poetWriter.writeIndentedLine("_response = self._requests_session.request( # type: ignore");
            poetWriter.increaseIndent();
            poetWriter.writeIndentedLine("'%s',", httpMethod());
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

        String myPyType();

        ParameterType paramType();

        class Builder extends ImmutablePythonEndpointParam.Builder {}

        static Builder builder() {
            return new Builder();
        }

    }
}
