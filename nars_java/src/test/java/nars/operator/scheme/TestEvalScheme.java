package nars.operator.scheme;


import nars.build.Default;
import nars.core.NewNAR;
import nars.io.condition.OutputContainsCondition;
import nars.logic.JavaNALTest;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

public class TestEvalScheme extends JavaNALTest {

    public TestEvalScheme(NewNAR build) {
        super(build);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default().setInternalExperience(null)}});
    }


    @Test
    public void testCAR() {

        //nar.addPlugin(new Scheme());

        nar.input("scheme((*, car, (*, quote, (*, 2, 3))), #x)!");

        //OUT: <(^scheme,(*,car,(*,quote,(*,2,3))),$1) =/> <$1 <-> 2>>. :|: %1.00;0.99% {0|0 : 1 : }

        nar.requires.add(new OutputContainsCondition(nar, "<(^scheme,(*,car,(*,quote,(*,2,3))),$1,SELF) =/> <$1 <-> 2>>. :|:", 1));

        nar.run(4);

    }
}