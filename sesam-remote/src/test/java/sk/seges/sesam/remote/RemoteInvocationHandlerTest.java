package sk.seges.sesam.remote;

import org.junit.Test;

public class RemoteInvocationHandlerTest {

    public interface TestInterface {
        void test1();
        int test2();
        Integer test3(Boolean b);
    }
}
