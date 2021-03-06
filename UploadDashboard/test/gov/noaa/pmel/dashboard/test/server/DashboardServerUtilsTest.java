/**
 * 
 */
package gov.noaa.pmel.dashboard.test.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import gov.noaa.pmel.dashboard.datatype.LonLatConverter;
import gov.noaa.pmel.dashboard.server.DashboardServerUtils;
import gov.noaa.pmel.dashboard.shared.DashboardUtils;
import gov.noaa.pmel.dashboard.server.util.LanguageUtils;

/**
 * Unit tests for methods in {@link gov.noaa.pmel.dashboard.server.DashboardServerUtils}
 * @author Karl Smith
 */
public class DashboardServerUtilsTest {

	/**
	 * Test method for {@link gov.noaa.pmel.dashboard.server.DashboardServerUtils#getKeyForName(java.lang.String)}.
	 */
	@Test
	public void testGetKeyForName() {
		assertEquals("xco2atmdryinterp", DashboardServerUtils.getKeyForName("xCO2_atm_dry_interp"));
		assertEquals("xco2atmdryinterp", DashboardServerUtils.getKeyForName("xCO2 Atm Dry Interp"));
		assertEquals("xco2atmdryinterp", DashboardServerUtils.getKeyForName("xCO2; Atm; Dry; Interp"));
		assertEquals("xco2atmdryinterp", DashboardServerUtils.getKeyForName("xCO2, Atm, Dry, Interp"));
		assertEquals("other", DashboardServerUtils.getKeyForName("(other)"));
		assertEquals("tempc", DashboardServerUtils.getKeyForName("Temp [" + LonLatConverter.DEGREE_SYMBOL + "C]"));
		assertEquals("tempc", DashboardServerUtils.getKeyForName("Temp [ºC]"));
		assertEquals( "k" + LanguageUtils.oUmlaut + "rtzinger", 
				DashboardServerUtils.getKeyForName("K" + LanguageUtils.OUmlaut + "rt*Zinger") );
	}

	/**
	 * Test method for {@link gov.noaa.pmel.dashboard.server.DashboardServerUtils#getDatasetIDFromName(java.lang.String)}.
	 */
	@Test
	public void testGetDatasetIDFromName() {
		assertEquals( "ATLANTIS120115300", 
				DashboardServerUtils.getDatasetIDFromName("Atlantis 12-01-15 3:00") );
		assertEquals( "N" + LanguageUtils.UUmlaut + "MBER20", 
				DashboardServerUtils.getDatasetIDFromName("\"N" + LanguageUtils.uUmlaut + 
						"mber 2' 0" + LonLatConverter.DEGREE_SYMBOL + "\t\"") );
	}

	/**
	 * Test method for {@link gov.noaa.pmel.dashboard.server.DashboardServerUtils#checkDatasetID(java.lang.String)}.
	 */
	@Test
	public void testCheckDatasetID() {
		assertEquals( "ATLANTIS120115300", 
				DashboardServerUtils.checkDatasetID("Atlantis120115300") );
		assertEquals( "N" + LanguageUtils.EGrave + "VER20", 
				DashboardServerUtils.checkDatasetID("N" + LanguageUtils.eGrave + "ver20") );
		boolean caught = false;
		try {
			DashboardServerUtils.checkDatasetID("Atlantis 12");
		} catch ( IllegalArgumentException ex ) {
			caught = true;
		}
		if ( ! caught ) {
			fail("Invalid character did not raise an Exception");
		}
		caught = false;
		try {
			DashboardServerUtils.checkDatasetID("Atlantis_12");
		} catch ( IllegalArgumentException ex ) {
			caught = true;
		}
		if ( ! caught ) {
			fail("Invalid character did not raise an Exception");
		}
		caught = false;
		try {
			DashboardServerUtils.checkDatasetID("Atlantis-12");
		} catch ( IllegalArgumentException ex ) {
			caught = true;
		}
		if ( ! caught ) {
			fail("Invalid character did not raise an Exception");
		}
	}

	/**
	 * Test method for {@link gov.noaa.pmel.dashboard.server.DashboardServerUtils#getMinMaxValidData(double[])}.
	 */
	@Test
	public void testGetMinMaxValidData() {
		final double[] times_353L20120107 = new double[] {
				1325963667, 1325975328, 1325976458, 1325978045, 1325984053, 
				1325989958, 1325990592, 1325993894, 1325995160, 1325999461, 1326014187, 
				1326016822, 1326023684, 1326073761, 1326076901, 1326078644, 1326083420, 
				1326090152, 1326093973, 1326095984, 1326101095, 1326104363, 1326111446, 
				1326118074, 1326133511, 1326188389, 1326241473, 1326245535, 1326262827, 
				1326275331, 1326311439, 1326315927, 1326335126, 1326337068, 1326339382, 
				1326345192, 1326353257, 1326363178, 1326369506 
			};
		final double[] fco2s_353L20120107 = new double [] {
				311.698952308972, 314.139407179203, 309.493714068311, 
				340.725606791838, 331.182362018347, 340.299382061299, 333.16643879323, 
				340.215060385161, 338.60108673904, 341.04194604536, 347.746126550073, 
				339.613000040417, DashboardUtils.FP_MISSING_VALUE, 356.698922379513, 351.043314012183, 
				348.885021386104, 353.092479423243, 363.608547595615, 361.604664466921, 
				DashboardUtils.FP_MISSING_VALUE, 370.15949545442, 378.127990322985, 387.469738780931, DashboardUtils.FP_MISSING_VALUE, 
				380.874314596119, 378.506086707137, 376.837157353496, 384.919664163242, 
				DashboardUtils.FP_MISSING_VALUE, 367.966155139812, 368.000699431963, 350.778403187297, DashboardUtils.FP_MISSING_VALUE, 
				368.338781682715, 358.249081384324, DashboardUtils.FP_MISSING_VALUE, 359.859678913034, 
				364.893364508143, 354.720805231615 
			};

		double[] minMax = DashboardServerUtils.getMinMaxValidData(times_353L20120107);
		assertEquals( 1325963667.0, minMax[0], 0.1 );
		assertEquals( 1326369506.0, minMax[1], 0.1 );
		minMax = DashboardServerUtils.getMinMaxValidData(fco2s_353L20120107);
		assertEquals( 309.493714068311, minMax[0], 1.0E-12 );
		assertEquals( 387.469738780931, minMax[1], 1.0E-12 );
	}

	/**
	 * Test method for {@link gov.noaa.pmel.dashboard.server.DashboardServerUtils#distanceBetween(double, double, double, double, double, double)}.
	 */
	@Test
	public void testDistanceBetween() {
		double[] lons = { -5.0, 15.0 };
		double lonDist = DashboardServerUtils.EARTH_AUTHALIC_RADIUS * Math.abs(lons[1] - lons[0]) * Math.PI / 180.0;
		double[] lats = { 60.0, 75.0 };
		double latDist = DashboardServerUtils.EARTH_AUTHALIC_RADIUS * Math.abs(lats[1] - lats[0]) * Math.PI / 180.0;

		double dist;
		// Check longitude distance along the equator 
		dist = DashboardServerUtils.distanceBetween(lons[0], 0.0, lons[1], 0.0);
		assertEquals(lonDist, dist, 1.0E-5);
		dist = DashboardServerUtils.distanceBetween(lons[1], 0.0, lons[0], 0.0);
		assertEquals(lonDist, dist, 1.0E-5);
		// Check latitude distance along a meridian
		dist = DashboardServerUtils.distanceBetween(lons[0], lats[0], lons[0], lats[1]);
		assertEquals(latDist, dist, 1.0E-5);
		dist = DashboardServerUtils.distanceBetween(lons[0], lats[1], lons[0], lats[0]);
		assertEquals(latDist, dist, 1.0E-5);
		double[] austin = { -97.74306, 30.26715 };
		double[] dallas = { -96.80667, 32.78306 };
		double expected = 158.5 * 1.8537936;  // 200.0 miles on interstate a few hundred feet above sea level
		dist = DashboardServerUtils.distanceBetween(austin[0], austin[1], dallas[0], dallas[1]);
		assertEquals(expected, dist, 1.0);
	}

}
