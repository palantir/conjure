package test;

import com.palantir.tokens.auth.AuthHeader;
import java.io.InputStream;
import java.lang.String;
import javax.annotation.Generated;

/** TestServiceDocs */
@Generated("com.palantir.conjure.gen.dialogue.DialogClientGenerator")
public interface BlockingTestService {
    /** stringDocs */
    String string(AuthHeader authHeader);

    String stringEcho(AuthHeader authHeader, String string);

    int integer(AuthHeader authHeader);

    int integerEcho(AuthHeader authHeader, int integer);

    String queryEcho(AuthHeader authHeader, int integer);

    Complex complex(AuthHeader authHeader);

    Complex complexEcho(AuthHeader authHeader, Complex complex);

    InputStream binaryEcho(AuthHeader authHeader, String string);
}
