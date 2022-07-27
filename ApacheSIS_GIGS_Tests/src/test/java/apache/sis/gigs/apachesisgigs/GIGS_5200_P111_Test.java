package apache.sis.gigs.apachesisgigs;

import java.io.File;
import org.junit.Test;

public class GIGS_5200_P111_Test {
    
    public final static String GIGS_TEST_DIRECTORY = "Path to GIGS test files";
    
    @Test
    public void test_GIGS_tfm_5201_GeogGeocen() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5201_GeogGeocen.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5203_PosVec_part1() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5203_PosVec_part1.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5203_PosVec_part2() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5203_PosVec_part2.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5204_CoordFrame_part1() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5204_CoordFrame_part1.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5204_CoordFrame_part2() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5204_CoordFrame_part2.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5205_MolBad_part1() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5205_MolBad_part1.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5205_MolBad_part2() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5205_MolBad_part2.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5206_Nadcon_part2() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5206_Nadcon_part2.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5206_Nadcon_part3() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5206_Nadcon_part3.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5207_NTv2_part1() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5207_NTv2_part1.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5207_NTv2_part2() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5207_NTv2_part2.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5208_LonRot() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5208_LonRot.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5211_3trnslt_Geocen() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5211_3trnslt_Geocen.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5212_3trnslt_Geog3D_EPSGconcat() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5212_3trnslt_Geog3D_EPSGconcat.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
    @Test
    public void test_GIGS_tfm_5213_3trnslt_Geog2D_EPSGconcat() throws Exception {
        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 5200 Coordinate transformation test data\\P111\\GIGS_tfm_5213_3trnslt_Geog2D_EPSGconcat.p111");
        P111TestRunner runner = new P111TestRunner();
        runner.runTest(file);
    }
    
}

