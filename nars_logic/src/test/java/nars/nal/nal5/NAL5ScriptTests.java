//package nars.nal.nal5;
//
//
//import nars.NARSeed;
//import nars.nal.ScriptNALTest;
//import nars.nar.Default;
//import org.junit.runners.Parameterized;
//
//import java.util.Collection;
//
//import static LibraryInput.getParams;
//
//public class NAL5ScriptTests extends ScriptNALTest {
//
//    public NAL5ScriptTests(NARSeed b, String input) {
//        super(b, input);
//    }
//
//    @Parameterized.Parameters(name= "{1} {0}")
//    public static Collection configurations() {
//        return getParams(new String[]{"test5"},
//                new Default(),
//                new Default().setInternalExperience(null),
//                new Default().setInternalExperience(null).level(5));
//    }
//
//
//    @Override
//    public int getMaxCycles() { return 900; }
//
//
//}
//
