package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.ValueBlock;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.grobid.core.engines.UnitParserIntegrationTest.initEngineForTests;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class QuantityParserIntegrationTest {
    QuantityParser target;

    @BeforeClass
    public static void init() throws Exception {
        initEngineForTests();
    }


    @Before
    public void setUp() {
        target = QuantityParser.getInstance(true);
    }

    @Test
    public void testQuantityParsing_sameAsBaseUnit() throws Exception {
        List<Measurement> measurements = target.process("It measured 10 meters in length.");

        assertThat(measurements, hasSize(1));
        Measurement measurement = measurements.get(0);
        assertThat(measurement.getType(), is(UnitUtilities.Measurement_Type.VALUE));
        assertThat(measurement.getQuantityAtomic().getRawValue(), is("10"));
        assertThat(measurement.getQuantityAtomic().getNormalizedQuantity().getRawValue(), is("10"));
        assertThat(measurement.getQuantityAtomic().getNormalizedQuantity().getValue(), is(new BigDecimal("10")));
    }

    @Test
    public void testQuantityParsing_differentThanBaseUnit() throws Exception {
        List<Measurement> measurements = target.process("We measured 10 km distance.");

        assertThat(measurements, hasSize(1));
        Measurement measurement = measurements.get(0);
        assertThat(measurement.getType(), is(UnitUtilities.Measurement_Type.VALUE));
        assertThat(measurement.getQuantityAtomic().getRawValue(), is("10"));
        assertThat(measurement.getQuantityAtomic().getNormalizedQuantity().getRawValue(), is("10"));
        assertThat(measurement.getQuantityAtomic().getNormalizedQuantity().getValue(), is(new BigDecimal("10000")));

    }

    @Test
    public void testQuantityParsing_differentThanBaseUnit_alfabeticalValue() throws Exception {
        List<Measurement> measurements = target.process("we have ran about ten km.");

        assertThat(measurements, hasSize(1));
        Measurement measurement = measurements.get(0);
        assertThat(measurement.getType(), is(UnitUtilities.Measurement_Type.VALUE));
        assertThat(measurement.getQuantityAtomic().getRawValue(), is("ten"));
        assertThat(measurement.getQuantityAtomic().getParsedValue().getNumeric().toPlainString(), is(BigDecimal.valueOf(10.0).toPlainString()));

        ValueBlock value = new ValueBlock();
        value.setRawValue("ten");
        value.setAlpha("ten");
        //Workaround to overcome the lack of equals() in OffsetPosition
        value.getAlpha().setOffsets(measurement.getQuantityAtomic().getParsedValue().getStructure().getAlpha().getOffsets());

        assertThat(measurement.getQuantityAtomic().getParsedValue().getStructure(), is(value));
        assertThat(measurement.getQuantityAtomic().getNormalizedQuantity().getRawValue(), is("ten"));
        assertThat(measurement.getQuantityAtomic().getNormalizedQuantity().getValue(), is(new BigDecimal("10000")));
    }

    @Test
    public void testQuantityParsing_composedUnit() throws Exception {
        List<Measurement> measurements = target.process("The result was 10 m -1 * s -1.");

        assertThat(measurements, hasSize(1));
        Measurement measurement = measurements.get(0);
        assertThat(measurement.getType(), is(UnitUtilities.Measurement_Type.VALUE));
        assertThat(measurement.getQuantityAtomic().getRawValue(), is("10"));
        assertThat(measurement.getQuantityAtomic().getNormalizedQuantity().getRawValue(), is("10"));
        assertThat(measurement.getQuantityAtomic().getNormalizedQuantity().getValue(), is(new BigDecimal("10")));
    }

    @Test
    public void testQuantityParser1() throws Exception {

        String text = IOUtils.toString(
            this.getClass().getResourceAsStream("/test1.txt"), UTF_8);
        System.out.println("\ntest1.txt\n" + text + "\n");

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }

    @Test
    public void testQuantityParser2() throws Exception {

        String text = IOUtils.toString(
            this.getClass().getResourceAsStream("/test2.txt"), UTF_8);
        System.out.println("\ntest2.txt\n" + text + "\n");

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }

    @Test
    public void testQuantityParser3() throws Exception {

        String text = IOUtils.toString(
            this.getClass().getResourceAsStream("/test0.training.txt"), UTF_8);

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }


    @Test
    public void testQuantityParserInterfaceExample1() throws Exception {
        String text = "A 20kg ingot is made in a high frequency induction melting furnace and forged to 30mm in thickness and " +
            "90mm in width at 850 to 1,150°C. Specimens No.2 to 4, 6 and 15 are materials embodying the invention. " +
            "Others are for comparison. No.1 is a material equivalent to ASTM standard A469-88 class 8 for generator rotor " +
            "shaft material. No. 5 is a material containing relatively high Al content. \n" +
            "\n" +
            "These specimens underwent heat treatment by simulating the conditions for the large size rotor shaft centre " +
            "of a large capacity generator. " +
            "First, it was heated to 840°C to form austenite structure and cooled at the speed of 100°C/hour to harden. " +
            "Then, the specimen was heated and held at 575 to 590°C for 32 hours and cooled at a speed of 15°C/hour. Tempering " +
            "was done at such a temperature to secure tensile strength in the range of 100 to 105kg/mm2 for each specimen.";
        List<Measurement> measurements = target.process(text);

        assertThat(measurements, hasSize(10));
        assertThat(measurements.get(4).getQuantityAtomic().getRawValue(), is("840"));
        assertThat(measurements.get(5).getQuantityAtomic().getRawValue(), is("100"));

        int offsetStart0 = measurements.get(4).getQuantityAtomic().getOffsetStart();
        int offsetEnd0 = measurements.get(4).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart0, offsetEnd0), is("840"));

        int offsetStart1 = measurements.get(5).getQuantityAtomic().getOffsetStart();
        int offsetEnd1 = measurements.get(5).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart1, offsetEnd1), is("100"));
    }

    @Test
    public void testQuantityParserInterfaceExample2() throws Exception {
        String text = "The cells were washed three times with RPMI1640  medium (Nissui Pharmaceutical Co.). " +
            "The cells (1 x107) were incubated in RPMI-1640 medium containing 10% calf fetal serum (Gibco Co.), " +
            "50 µg/ml streptomycin, 50 IU/ml of penicillin, 2-mercaptoethanol (5 x 10-5 M), sheep red blood " +
            "cells (5 x 106 cells) and a test compound dissolved in dimethyl sulfoxide supplied on a microculture " +
            "plate (NUNC Co., 24 wells) in a carbon dioxide gas incubator (TABAI ESPEC CORP) at 37°C for 5 days.\n" +
            "\n" +
            "A solution of 1.18 g (4.00 mmols) of the Compound a obtained in Reference Example 1, 0.39 g (4.13 mmols) " +
            "of 4-aminopyridine and 20 ml of toluene was heated to reflux for 2 hours. After cooling, the reaction " +
            "mixture was poured into 1 N sodium hydroxide aqueous solution, and washed twice with chloroform. " +
            "2 N Hydrochloric acid aqueous solution was added to the aqueous layer and the precipitated white crystals " +
            "were filtered and dried to give 0.73 g (yield: 53%) of Compound 3.";
        List<Measurement> measurements = target.process(text);

        assertThat(measurements, hasSize(17));
        assertThat(measurements.get(0).getQuantityAtomic().getParsedValue().getNumeric().toPlainString(), is("3.0"));
        assertThat(measurements.get(5).getQuantityAtomic().getRawValue(), is("5 x 10-5"));
        assertThat(measurements.get(5).getQuantityAtomic().getParsedValue().getNumeric().toPlainString(), is("0.0000500000"));

        int offsetStart0 = measurements.get(4).getQuantityAtomic().getOffsetStart();
        int offsetEnd0 = measurements.get(4).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart0, offsetEnd0), is("50"));

        int offsetStart1 = measurements.get(9).getQuantityAtomic().getOffsetStart();
        int offsetEnd1 = measurements.get(9).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart1, offsetEnd1), is("1.18"));

        int offsetStartFull = measurements.get(9).getRawOffsets().start;
        int offsetEndFull = measurements.get(9).getRawOffsets().end;

        assertThat(text.substring(offsetStartFull, offsetEndFull), is("1.18 g"));

        assertThat(measurements.get(9).getRawString(), is("1.18 g"));
    }

    @Test
    public void testQuantityParserInterfaceExample3() throws Exception {
        String text = "Fifty-three journals were collected: 13 were eliminated from analysis, because they were " +
            "incomplete, unclear or unreadable. 40 journals were analysed: 19 were journals of subjects of race Z " +
            "(4 women and 15 men, 30 ± 10 years, 176 ± 7 cm, 70 ± 9 kg, 15 ± 5 % of fat mass, VO 2max : 50 ± 8 ml · " +
            "kg −1 · min −1 and 21 of race A (6 women and 15 men, 40 ± 7 years, 176 ± 7 cm, 72 ± 10 kg, 18 ± 8 % fat " +
            "mass, VO 2max : 58 ± 8 ml · kg −1 · min −1 ). Energy, macronutrients (CHO, fat and proteins) and liquid " +
            "intakes were analysed.";

        List<Measurement> measurements = target.process(text);

        assertThat(measurements, hasSize(19));
        assertThat(measurements.get(0).getQuantityAtomic().getRawValue(), is("Fifty-three"));
        assertThat(measurements.get(0).getQuantityAtomic().getParsedValue().getNumeric().toPlainString(), is("53.0"));

        int offsetQuantityStart0 = measurements.get(6).getQuantityBase().getOffsetStart();
        int offsetQuantityEnd0 = measurements.get(6).getQuantityBase().getOffsetEnd();
        int offsetQuantityStart1 = measurements.get(6).getQuantityRange().getOffsetEnd();
        int offsetQuantityEnd1 = measurements.get(6).getQuantityRange().getOffsetEnd();

        assertThat(text.substring(offsetQuantityStart0, offsetQuantityEnd1), is("30 ± 10"));

        assertThat(measurements.get(6).getRawString(), is("30 ± 10 years"));
        assertThat(text.substring(measurements.get(6).getRawOffsets().start, measurements.get(6).getRawOffsets().end), is("30 ± 10 years"));
    }

    @Test
    public void testQuantityParserInterfaceExample4() throws Exception {
        String text = "COS-7 cells transfected with the indicated plasmids were lysed in Laemmli sample buffer or " +
            "the lysis buffer mentioned above. E18.5 mouse brains (ICR) were homogenized in 20 mm HEPES (pH 7.4), " +
            "0.1 mm EDTA, 0.1 mm EGTA, 150 mm NaCl, 2 mm MgCl2, 1 mm Na3VO4, 0.4 mm 4-(2-aminoethyl)benzenesulfonyl " +
            "fluoride hydrochloride, 10 μg/ml leupeptin, and 1 mm dithiothreitol with a Teflon pestle homogenizer. The " +
            "lysates or homogenates were centrifuged at 15,000 × g for 20 min, and the supernatants were used for " +
            "immunoprecipitation of Cdk5 with anti-Cdk5 (C8) or anti-p35 (C19). In some cases, immunoprecipitation was " +
            "performed with anti-Cdk5 (C8) or anti-p35 (C19) that had been cross-linked to protein A-Sepharose beads " +
            "using the Pierce Crosslink IP kit according to the protocol of the manufacturer. The cell extracts were " +
            "incubated with 1.5 μg of antibody and 20 μl of protein A-Sepharose beads and rotated overnight at 4 °C. " +
            "The beads were washed with washing buffer (25 mm Tris-HCl (pH 7.5), 0.1 mm EDTA, 0.1 mm EGTA, 500 mm NaCl, " +
            "0.5% Nonidet P-40, and 1 mm dithiothreitol) five times. The kinase activity of Cdk5 was measured with " +
            "histone H1 as a substrate in kinase buffer (10 mm MOPS (pH 6.8), 1 mm MgCl2, 0.1 mm EDTA, and 0.1 mm " +
            "EGTA) at 37 °C for 30 min. After SDS-PAGE, phosphorylation was visualized by autoradiography with an " +
            "imaging plate.";
        List<Measurement> measurements = target.process(text);

        assertThat(measurements, hasSize(30));
        assertThat(measurements.get(4).getQuantityAtomic().getRawValue(), is("150"));
        assertThat(measurements.get(5).getQuantityAtomic().getRawValue(), is("2"));

        int offsetStart0 = measurements.get(4).getQuantityAtomic().getOffsetStart();
        int offsetEnd0 = measurements.get(4).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart0, offsetEnd0), is("150"));

        int offsetStart1 = measurements.get(5).getQuantityAtomic().getOffsetStart();
        int offsetEnd1 = measurements.get(5).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart1, offsetEnd1), is("2"));
    }

    @Test
    public void testQuantityParser5() throws Exception {
        String text = "First, it was heated to 840°C to form austenite structure and cooled \n\nat the speed of 100°C/hour to harden.";

        List<Measurement> measurements = target.process(text);

        assertThat(measurements, hasSize(2));
        assertThat(measurements.get(0).getQuantityAtomic().getRawValue(), is("840"));
        assertThat(measurements.get(1).getQuantityAtomic().getRawValue(), is("100"));

        int offsetStart0 = measurements.get(0).getQuantityAtomic().getOffsetStart();
        int offsetEnd0 = measurements.get(0).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart0, offsetEnd0), is("840"));

        int offsetStart1 = measurements.get(1).getQuantityAtomic().getOffsetStart();
        int offsetEnd1 = measurements.get(1).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart1, offsetEnd1), is("100"));
    }


//    @Test
//    public void test() throws Exception  {
//        String text = "Before the 1920s the number of stages was usually 15 at most and the riders enjoyed at least one day of rest after each stage.";
//
//        target.process(text);
//
//    }

    @Test
    public void testQuantityParser_particularCaseWhereIntervalsAreMerged() throws Exception {

        String text = "Before the 1920s the number of stages was usually 15 at most and the riders enjoyed at least one day of rest after each stage.";

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        String result = "Before\tbefore\tB\tBe\tBef\tBefo\te\tre\tore\tfore\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "1920\t1920\t1\t19\t192\t1920\t0\t20\t920\t1920\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdddd\td\t0\t0\tI-<valueMost>\n" +
            "s\ts\ts\ts\ts\ts\ts\ts\ts\ts\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<valueMost>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "number\tnumber\tn\tnu\tnum\tnumb\tr\ter\tber\tmber\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "stages\tstages\ts\tst\tsta\tstag\ts\tes\tges\tages\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "usually\tusually\tu\tus\tusu\tusua\ty\tly\tlly\tally\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "15\t15\t1\t15\t15\t15\t5\t15\t15\t15\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
            "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "most\tmost\tm\tmo\tmos\tmost\tt\tst\tost\tmost\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "riders\triders\tr\tri\trid\tride\ts\trs\ters\tders\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "enjoyed\tenjoyed\te\ten\tenj\tenjo\td\ted\tyed\toyed\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "least\tleast\tl\tle\tlea\tleas\tt\tst\tast\teast\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "one\tone\to\ton\tone\tone\te\tne\tone\tone\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t1\tI-<valueLeast>\n" +
            "day\tday\td\tda\tday\tday\ty\tay\tday\tday\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\tI-<unitLeft>\n" +
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "rest\trest\tr\tre\tres\trest\tt\tst\test\trest\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "after\tafter\ta\taf\taft\tafte\tr\ter\tter\tfter\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "each\teach\te\tea\teac\teach\th\tch\tach\teach\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "stage\tstage\ts\tst\tsta\tstag\te\tge\tage\ttage\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>";

        List<Measurement> measurementList = target.extractMeasurement(tokens, result, target.getSentencesOffsets(tokens));

        assertThat(measurementList, hasSize(3));
    }

    @Test
    public void testEntitiesExtractionWithParticularCase() throws Exception {
        String text = "Through EPD technique, we can increase the coercivity by more than 6.5 kOe with less than 1.2 wt.% Dy.";

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        String result = "Through\tthrough\tT\tTh\tThr\tThro\th\tgh\tugh\tough\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
            "EPD\tepd\tE\tEP\tEPD\tEPD\tD\tPD\tEPD\tEPD\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXXX\tX\t0\t0\t<other>\n" +
            "technique\ttechnique\tt\tte\ttec\ttech\te\tue\tque\tique\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "we\twe\tw\twe\twe\twe\te\twe\twe\twe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "can\tcan\tc\tca\tcan\tcan\tn\tan\tcan\tcan\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "increase\tincrease\ti\tin\tinc\tincr\te\tse\tase\tease\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "coercivity\tcoercivity\tc\tco\tcoe\tcoer\ty\tty\tity\tvity\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "by\tby\tb\tby\tby\tby\ty\tby\tby\tby\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "more\tmore\tm\tmo\tmor\tmore\te\tre\tore\tmore\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "than\tthan\tt\tth\ttha\tthan\tn\tan\than\tthan\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "6\t6\t6\t6\t6\t6\t6\t6\t6\t6\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueLeast>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueLeast>\n" +
            "5\t5\t5\t5\t5\t5\t5\t5\t5\t5\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueLeast>\n" +
            "kOe\tkoe\tk\tkO\tkOe\tkOe\te\tOe\tkOe\tkOe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXx\txXx\t1\t0\tI-<unitLeft>\n" +
            "with\twith\tw\twi\twit\twith\th\tth\tith\twith\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "less\tless\tl\tle\tles\tless\ts\tss\tess\tless\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "than\tthan\tt\tth\ttha\tthan\tn\tan\than\tthan\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueMost>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueMost>\n" +
            "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueMost>\n" +
            "wt\twt\tw\twt\twt\twt\tt\twt\twt\twt\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\tI-<unitLeft>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<unitLeft>\n" +
            "%\t%\t%\t%\t%\t%\t%\t%\t%\t%\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t%\t%\t0\t0\t<unitLeft>\n" +
            "Dy\tdy\tD\tDy\tDy\tDy\ty\tDy\tDy\tDy\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n";

        List<Measurement> measurementList = target.extractMeasurement(tokens, result, target.getSentencesOffsets(tokens));

        assertThat(measurementList, hasSize(2));
        assertThat(measurementList.get(0).getQuantityLeast().getRawValue(), is("6.5"));
        assertThat(measurementList.get(0).getQuantityLeast().getRawUnit().getRawName(), is("kOe"));
        assertThat(measurementList.get(1).getQuantityMost().getRawValue(), is("1.2"));
        assertThat(measurementList.get(1).getQuantityMost().getRawUnit().getRawName(), is("wt.%"));
    }

    @Test
    public void testReconstructionWithSentenceTokenizer() throws Exception {

        String text = "Before the 1920s the number of stages was usually 15 at most. The riders enjoyed at least one day of rest after each stage.";

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        String result = "Before\tbefore\tB\tBe\tBef\tBefo\te\tre\tore\tfore\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "1920\t1920\t1\t19\t192\t1920\t0\t20\t920\t1920\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdddd\td\t0\t0\tI-<valueMost>\n" +
            "s\ts\ts\ts\ts\ts\ts\ts\ts\ts\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<valueMost>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "number\tnumber\tn\tnu\tnum\tnumb\tr\ter\tber\tmber\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "stages\tstages\ts\tst\tsta\tstag\ts\tes\tges\tages\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "usually\tusually\tu\tus\tusu\tusua\ty\tly\tlly\tally\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "15\t15\t1\t15\t15\t15\t5\t15\t15\t15\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
            "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "most\tmost\tm\tmo\tmos\tmost\tt\tst\tost\tmost\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            ".\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "The\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "riders\triders\tr\tri\trid\tride\ts\trs\ters\tders\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "enjoyed\tenjoyed\te\ten\tenj\tenjo\td\ted\tyed\toyed\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "least\tleast\tl\tle\tlea\tleas\tt\tst\tast\teast\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "one\tone\to\ton\tone\tone\te\tne\tone\tone\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t1\tI-<valueLeast>\n" +
            "day\tday\td\tda\tday\tday\ty\tay\tday\tday\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\tI-<unitLeft>\n" +
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "rest\trest\tr\tre\tres\trest\tt\tst\test\trest\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "after\tafter\ta\taf\taft\tafte\tr\ter\tter\tfter\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "each\teach\te\tea\teac\teach\th\tch\tach\teach\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "stage\tstage\ts\tst\tsta\tstag\te\tge\tage\ttage\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>";

        List<OffsetPosition> sentences = Arrays.asList(new OffsetPosition(0, 61), new OffsetPosition(61, 123));
        List<Measurement> measurementList = target.extractMeasurement(tokens, result, sentences);

        assertThat(measurementList, hasSize(3));

        assertThat(measurementList.get(0).getQuantityMost().getRawValue(), is("1920s"));
        assertThat(measurementList.get(1).getQuantityMost().getRawValue(), is("15"));
        assertThat(measurementList.get(2).getQuantityLeast().getRawValue(), is("one"));
    }


    @Test
    public void testReconstrictuingListWithMiddleUnit2() throws Exception {
        String text = "The acidity was pH 1, 3, 4, 5 and 6 and it was correlated with interesting power of 23 W.";

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        String result = "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
            "acidity\tacidity\ta\tac\taci\tacid\ty\tty\tity\tdity\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "pH\tph\tp\tpH\tpH\tpH\tH\tpH\tpH\tpH\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txX\txX\t1\t0\tI-<unitRight>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "3\t3\t3\t3\t3\t3\t3\t3\t3\t3\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "4\t4\t4\t4\t4\t4\t4\t4\t4\t4\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "5\t5\t5\t5\t5\t5\t5\t5\t5\t5\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "6\t6\t6\t6\t6\t6\t6\t6\t6\t6\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "it\tit\ti\tit\tit\tit\tt\tit\tit\tit\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "correlated\tcorrelated\tc\tco\tcor\tcorr\td\ted\tted\tated\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "with\twith\tw\twi\twit\twith\th\tth\tith\twith\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "interesting\tinteresting\ti\tin\tint\tinte\tg\tng\ting\tting\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "power\tpower\tp\tpo\tpow\tpowe\tr\ter\twer\tower\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t1\t0\t<other>\n" +
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "23\t23\t2\t23\t23\t23\t3\t23\t23\t23\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
            "W\tw\tW\tW\tW\tW\tW\tW\tW\tW\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\tI-<unitLeft>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>";

        List<Measurement> measurementList = target.extractMeasurement(tokens, result);

        assertThat(measurementList, hasSize(2));

    }


    @Test
    public void testReconstrictuingListWithMiddleUnit() throws Exception {

        String text = "Taking T c ¼ 2:30, 1.79, and 1.51 K, we obtain 1.36, 1.01, \n" +
            "and 0.47 T for their corresponding pressures. In the \n" +
            "Ginzburg-Landau (GL) theory, H c2 ¼ È 0 =2 2 , where \n" +
            "is the coherence length and is proportional to \n" +
            "ffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffi \n" +
            "ð1 þ t 2 Þ=ð1 À t 2 Þ \n" +
            "p \n" +
            ", È 0 is the flux quantum, and t ¼ T =T c \n" +
            "is the reduced temperature. Combining terms gives \n" +
            "\n";

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        String result = "Taking\ttaking\tT\tTa\tTak\tTaki\tg\tng\ting\tking\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<other>\n" +
            "c\tc\tc\tc\tc\tc\tc\tc\tc\tc\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<other>\n" +
            "¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t¼\t¼\t0\t0\t<other>\n" +
            "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            ":\t:\t:\t:\t:\t:\t:\t:\t:\t:\tALLCAPS\tNODIGIT\t1\tPUNCT\t:\t:\t0\t0\t<valueList>\n" +
            "30\t30\t3\t30\t30\t30\t0\t30\t30\t30\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueList>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueList>\n" +
            "79\t79\t7\t79\t79\t79\t9\t79\t79\t79\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueList>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueList>\n" +
            "51\t51\t5\t51\t51\t51\t1\t51\t51\t51\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueList>\n" +
            "K\tk\tK\tK\tK\tK\tK\tK\tK\tK\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\tI-<unitLeft>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "we\twe\tw\twe\twe\twe\te\twe\twe\twe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "obtain\tobtain\to\tob\tobt\tobta\tn\tin\tain\ttain\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueList>\n" +
            "36\t36\t3\t36\t36\t36\t6\t36\t36\t36\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueList>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueList>\n" +
            "01\t01\t0\t01\t01\t01\t1\t01\t01\t01\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueList>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueList>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueList>\n" +
            "47\t47\t4\t47\t47\t47\t7\t47\t47\t47\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueList>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\tI-<unitLeft>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "their\ttheir\tt\tth\tthe\tthei\tr\tir\teir\their\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "corresponding\tcorresponding\tc\tco\tcor\tcorr\tg\tng\ting\tding\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "pressures\tpressures\tp\tpr\tpre\tpres\ts\tes\tres\tures\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
            "In\tin\tI\tIn\tIn\tIn\tn\tIn\tIn\tIn\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "Ginzburg\tginzburg\tG\tGi\tGin\tGinz\tg\trg\turg\tburg\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
            "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
            "Landau\tlandau\tL\tLa\tLan\tLand\tu\tau\tdau\tndau\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
            "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
            "GL\tgl\tG\tGL\tGL\tGL\tL\tGL\tGL\tGL\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXX\tX\t0\t0\t<other>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
            "theory\ttheory\tt\tth\tthe\ttheo\ty\try\tory\teory\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "H\th\tH\tH\tH\tH\tH\tH\tH\tH\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<other>\n" +
            "c\tc\tc\tc\tc\tc\tc\tc\tc\tc\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<other>\n" +
            "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
            "¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t¼\t¼\t0\t0\t<other>\n" +
            "È\tè\tÈ\tÈ\tÈ\tÈ\tÈ\tÈ\tÈ\tÈ\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t0\t0\t<other>\n" +
            "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
            "=\t=\t=\t=\t=\t=\t=\t=\t=\t=\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t=\t=\t0\t0\t<other>\n" +
            "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueAtomic>\n" +
            "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueAtomic>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "where\twhere\tw\twh\twhe\twher\te\tre\tere\there\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "is\tis\ti\tis\tis\tis\ts\tis\tis\tis\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "coherence\tcoherence\tc\tco\tcoh\tcohe\te\tce\tnce\tence\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "length\tlength\tl\tle\tlen\tleng\th\tth\tgth\tngth\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "is\tis\ti\tis\tis\tis\ts\tis\tis\tis\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "proportional\tproportional\tp\tpr\tpro\tprop\tl\tal\tnal\tonal\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "ffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffi\tffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffiffi\tf\tff\tffi\tffif\ti\tfi\tffi\tiffi\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "ð1\tð1\tð\tð1\tð1\tð1\t1\tð1\tð1\tð1\tNOCAPS\tCONTAINDIGIT\t0\tNOPUNCT\txd\txd\t0\t0\t<other>\n" +
            "þ\tþ\tþ\tþ\tþ\tþ\tþ\tþ\tþ\tþ\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
            "t\tt\tt\tt\tt\tt\tt\tt\tt\tt\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<other>\n" +
            "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
            "Þ\tþ\tÞ\tÞ\tÞ\tÞ\tÞ\tÞ\tÞ\tÞ\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t0\t0\t<other>\n" +
            "=\t=\t=\t=\t=\t=\t=\t=\t=\t=\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t=\t=\t0\t0\t<other>\n" +
            "ð1\tð1\tð\tð1\tð1\tð1\t1\tð1\tð1\tð1\tNOCAPS\tCONTAINDIGIT\t0\tNOPUNCT\txd\txd\t0\t0\t<other>\n" +
            "À\tà\tÀ\tÀ\tÀ\tÀ\tÀ\tÀ\tÀ\tÀ\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t0\t0\t<other>\n" +
            "t\tt\tt\tt\tt\tt\tt\tt\tt\tt\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<other>\n" +
            "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
            "Þ\tþ\tÞ\tÞ\tÞ\tÞ\tÞ\tÞ\tÞ\tÞ\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t0\t0\t<other>\n" +
            "p\tp\tp\tp\tp\tp\tp\tp\tp\tp\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<other>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "È\tè\tÈ\tÈ\tÈ\tÈ\tÈ\tÈ\tÈ\tÈ\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t0\t0\t<other>\n" +
            "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
            "is\tis\ti\tis\tis\tis\ts\tis\tis\tis\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "flux\tflux\tf\tfl\tflu\tflux\tx\tux\tlux\tflux\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "quantum\tquantum\tq\tqu\tqua\tquan\tm\tum\ttum\tntum\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "t\tt\tt\tt\tt\tt\tt\tt\tt\tt\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<other>\n" +
            "¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\t¼\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t¼\t¼\t0\t0\t<other>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<other>\n" +
            "=\t=\t=\t=\t=\t=\t=\t=\t=\t=\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t=\t=\t0\t0\t<other>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<other>\n" +
            "c\tc\tc\tc\tc\tc\tc\tc\tc\tc\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<other>\n" +
            "is\tis\ti\tis\tis\tis\ts\tis\tis\tis\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "reduced\treduced\tr\tre\tred\tredu\td\ted\tced\tuced\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "temperature\ttemperature\tt\tte\ttem\ttemp\te\tre\ture\tture\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
            "Combining\tcombining\tC\tCo\tCom\tComb\tg\tng\ting\tning\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
            "terms\tterms\tt\tte\tter\tterm\ts\tms\trms\terms\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "gives\tgives\tg\tgi\tgiv\tgive\ts\tes\tves\tives\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>";

        List<OffsetPosition> sentences = new ArrayList<>();
        sentences.add(new OffsetPosition(0, 105));
        sentences.add(new OffsetPosition(106, 410));
        sentences.add(new OffsetPosition(411, 432));
        List<Measurement> measurementList = target.extractMeasurement(tokens, result, sentences);

        assertThat(measurementList, hasSize(3));

        assertThat(measurementList.get(0).getQuantityList().get(0).getRawValue(), is("2:30"));
        assertThat(measurementList.get(0).getQuantityList().get(1).getRawValue(), is("1.79"));
        assertThat(measurementList.get(0).getQuantityList().get(2).getRawValue(), is("1.51"));

        assertThat(measurementList.get(1).getQuantityList().get(0).getRawValue(), is("1.36"));
        assertThat(measurementList.get(1).getQuantityList().get(1).getRawValue(), is("1.01"));
        assertThat(measurementList.get(1).getQuantityList().get(2).getRawValue(), is("0.47"));

        assertThat(measurementList.get(2).getQuantityAtomic().getRawValue(), is("2 2"));
    }


}