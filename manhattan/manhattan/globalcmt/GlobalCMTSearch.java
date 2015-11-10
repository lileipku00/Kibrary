package manhattan.globalcmt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.math.NumberUtils;

import manhattan.template.Location;

/**
 * 
 * Global CMT searchを行う時のQuery
 * 
 * @author Kensuke
 * @version 0.0.1
 * 
 * @version 0.0.2
 * @since 2013/9/20
 * 
 * @version 0.1.0
 * @since 2014/1/12 install all queries startDate の時刻は0:0:0.000
 *        endDateの時刻は23:59:59.999 にする
 * 
 * @since 2014/9/8
 * @version 0.1.1 fix
 * 
 * @version 0.1.2
 * @since 2015/1/29 Java 8 grammer
 * 
 * @version 0.1.5
 * @since 2015/2/12 {@link java.util.Calendar} &rarr;
 *        {@link java.time.LocalDate}
 * 
 * @version 0.1.5.1
 * @since 2015/4/15 minor bugs fixed
 * 
 * 
 * @version 0.1.6
 * @since 2015/8/21 time range is installed
 * 
 * @version 0.1.7
 * @since 2015/9/8 search Array &rarr; Set
 * 
 * 
 */
public class GlobalCMTSearch {

	private static DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	/**
	 * show date and time of event id
	 * 
	 * @param id
	 */
	private static void printIDinformation(GlobalCMTID id) {
		GlobalCMTData event = id.getEvent();
		Location location = event.getCmtLocation();
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		double depth = Math.round((6371 - location.getR()) * 1000) / 1000.0;
		System.out.println(id + " " + event.getCMTTime().format(outputFormat) + " " + lat + " " + lon + " " + depth);

	}

	/**
	 * @param predicate
	 *            {@link Predicate} for Event data of global CMT IDs
	 * @return all global CMT IDs satisfying the input predicate
	 */
	public static Set<GlobalCMTID> search(Predicate<GlobalCMTData> predicate) {
		return GlobalCMTCatalog.allNDK().stream().filter(predicate).map(n -> n.getGlobalCMTID())
				.collect(Collectors.toSet());
	}

	public static void setOutputFormat(DateTimeFormatter outputFormat) {
		GlobalCMTSearch.outputFormat = outputFormat;
	}

	/**
	 * end date
	 */
	private LocalDate endDate;

	/**
	 * end time for CMT time
	 */
	private LocalTime endTime;

	/**
	 * the lower limit of centroid time shift Default: -9999
	 */
	private double lowerCentroidTimeShift = -9999;

	/**
	 * the lower limit of depth range Default: 0
	 */
	private double lowerDepth = 0;

	/**
	 * the lower limit of latitude range [-90:90] Default: -90
	 */
	private double lowerLatitude = -90;

	/**
	 * the lower limit of longitude range [-180:180] Default: -180
	 */
	private double lowerLongitude = -180;

	/**
	 * the lower limit of bodywave magnitude Default: 0
	 */
	private double lowerMb = 0;

	/**
	 * the lower limit of surface wave magnitude Default: 0
	 */
	private double lowerMs = 0;

	/**
	 * the lower limit of moment magnitude Default: 0
	 */
	private double lowerMw = 0;

	/**
	 * the lower limit of null axis plunge [0, 90] (degree) Default: 0
	 */
	private int lowerNullAxisPlunge = 0;

	/**
	 * the lower limit of tension axis plunge [0, 90] (degree) Default: 0
	 */
	private int lowerTensionAxisPlunge;

	/**
	 * start date
	 */
	private LocalDate startDate;

	/**
	 * start time for CMT time
	 */
	private LocalTime startTime;

	/**
	 * the upper limit of centroid time shift Default: 9999
	 */
	private double upperCentroidTimeShift = 9999;

	/**
	 * the upper limit of depth range Default: 1000
	 */
	private double upperDepth = 1000;

	/**
	 * the upper limit of latitude range [-90:90] Default: 90
	 */
	private double upperLatitude = 90;

	/**
	 * the upper limit of longitude range [-180:180] Default: 180
	 */
	private double upperLongitude = 180;

	/**
	 * the upper limit of bodywave magnitude Default: 10
	 */
	private double upperMb = 10;

	/**
	 * the upper limit of surface wave magnitude Default: 10
	 * 
	 */
	private double upperMs = 10;

	/**
	 * the upper limit of moment magnitude Default: 10
	 */
	private double upperMw = 10;

	/**
	 * the upper limit of null axis plunge [0, 90] (degree) Default: 90
	 */
	private int upperNullAxisPlunge = 90;

	/**
	 * the upper limit of tension axis plunge [0, 90] (degree) Default: 90
	 */
	private int upperTensionAxisPlunge = 90;

	/**
	 * Search on 1 day.
	 * 
	 * @param startDate
	 *            on which this searches
	 */
	public GlobalCMTSearch(LocalDate startDate) {
		this(startDate, startDate);
	}

	/**
	 * Search from the startDate to endDate
	 * 
	 * @param startDate
	 *            starting date of the search (included)
	 * @param endDate
	 *            end date of the search (included)
	 */
	public GlobalCMTSearch(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public double getLowerCentroidTimeShift() {
		return lowerCentroidTimeShift;
	}

	public double getLowerDepth() {
		return lowerDepth;
	}

	public double getLowerLatitude() {
		return lowerLatitude;
	}

	public double getLowerLongitude() {
		return lowerLongitude;
	}

	public double getLowerMb() {
		return lowerMb;
	}

	public double getLowerMs() {
		return lowerMs;
	}

	public double getLowerMw() {
		return lowerMw;
	}

	public int getLowerNullAxisPlunge() {
		return lowerNullAxisPlunge;
	}

	public int getLowerTensionAxisPlunge() {
		return lowerTensionAxisPlunge;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public double getUpperCentroidTimeShift() {
		return upperCentroidTimeShift;
	}

	public double getUpperDepth() {
		return upperDepth;
	}

	public double getUpperLatitude() {
		return upperLatitude;
	}

	public double getUpperLongitude() {
		return upperLongitude;
	}

	public double getUpperMb() {
		return upperMb;
	}

	public double getUpperMs() {
		return upperMs;
	}

	public double getUpperMw() {
		return upperMw;
	}

	public int getUpperNullAxisPlunge() {
		return upperNullAxisPlunge;
	}

	public int getUpperTensionAxisPlunge() {
		return upperTensionAxisPlunge;
	}

	/**
	 * @return Set of {@link GlobalCMTID} which fulfill queries
	 */
	public Set<GlobalCMTID> search() {
		return GlobalCMTCatalog.allNDK().parallelStream().filter(ndk -> ndk.fulfill(this)).map(ndk -> ndk.getID())
				.collect(Collectors.toSet());
	}

	/**
	 * @return select an id
	 */
	public GlobalCMTID select() {
		GlobalCMTID[] ids = search().toArray(new GlobalCMTID[0]);
		if (ids.length == 0)
			return null;
		if (ids.length == 1)
			return ids[0];
		GlobalCMTID id = null;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new CloseShieldInputStream(System.in)));) {
			System.out.println("Which ID do you want to use?");
			System.out.println("# ID date time latitude longitude depth");
			for (int i = 0; i < ids.length; i++) {
				System.out.print(i + " ");
				printIDinformation(ids[i]);
			}
			// byte[] inputByte = new byte[4];
			int k = -1;
			while (k < 0) {
				String numStr = br.readLine();
				if (NumberUtils.isNumber(numStr))
					k = Integer.parseInt(numStr);
				if (k < 0 || ids.length <= k) {
					System.out.println("... which one? " + 0 + " - " + (ids.length - 1));
					k = -1;
				}
			}
			id = ids[k];
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println(id);
		return id;

	}

	/**
	 * Set centroid timeshift range
	 * 
	 * @param lowerCentroidTimeShift
	 *            lower limit of centroid time shift
	 * @param upperCentroidTimeShift
	 *            upper limit of centroid time shift
	 */
	public void setCentroidTimeShiftRange(double lowerCentroidTimeShift, double upperCentroidTimeShift) {
		if (upperCentroidTimeShift < lowerCentroidTimeShift)
			throw new RuntimeException("Input centroid time shift range is invalid");
		this.lowerCentroidTimeShift = lowerCentroidTimeShift;
		this.upperCentroidTimeShift = upperCentroidTimeShift;
	}

	/**
	 * Set depth range (<b>NOT</b> radius)
	 * 
	 * @param lowerDepth
	 *            [km] lower limit of depth
	 * @param upperDepth
	 *            [km] upper limit of depth
	 */
	public void setDepthRange(double lowerDepth, double upperDepth) {
		if (lowerDepth < 0 || upperDepth < lowerDepth)
			throw new IllegalArgumentException("input depth range is invalid");
		this.lowerDepth = lowerDepth;
		this.upperDepth = upperDepth;
	}

	/**
	 * CMT time must be before endTime
	 * 
	 * @param endTime
	 *            the endTime of search range.
	 */
	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	/**
	 * Latitude range<br>
	 * Default:[-90:90]<br>
	 * If you do not want to set a min or max, -90 or 90
	 * 
	 * if invalid values are input, {@link IllegalArgumentException}
	 * 
	 * @param lowerLatitude
	 *            [deg] [-90, upperLatitude)
	 * @param upperLatitude
	 *            [deg] (lowerLatitude, 90]
	 */
	public void setLatitudeRange(double lowerLatitude, double upperLatitude) {
		if (lowerLatitude < -90 || upperLatitude < lowerLatitude || 90 < upperLatitude)
			throw new IllegalArgumentException("Input latitude range is invalid");
		this.lowerLatitude = lowerLatitude;
		this.upperLatitude = upperLatitude;
	}

	/**
	 * Longitude range<br>
	 * Default:[-180:180]<br>
	 * 
	 * @param lowerLongitude
	 *            [-180, upperLongitude or 180)
	 * @param upperLongitude
	 *            (lowerLongitude, 360)
	 */
	public void setLongitudeRange(double lowerLongitude, double upperLongitude) {
		if (upperLongitude < lowerLongitude || 180 <= lowerLongitude || lowerLongitude < -180 || 360 < upperLongitude)
			throw new IllegalArgumentException("Invalid longitude range.");
		this.lowerLongitude = lowerLongitude;
		this.upperLongitude = upperLongitude;
	}

	public void setLowerNullAxisPlunge(int lowerNullAxisPlunge) {
		this.lowerNullAxisPlunge = lowerNullAxisPlunge;
	}

	public void setLowerTensionAxisPlunge(int lowerTensionAxisPlunge) {
		this.lowerTensionAxisPlunge = lowerTensionAxisPlunge;
	}

	/**
	 * Set mb range
	 * 
	 * @param lowerMb
	 *            lower limit of Mb
	 * @param upperMb
	 *            upper limit of Mb
	 */
	public void setMbRange(double lowerMb, double upperMb) {
		if (upperMb < lowerMb)
			throw new RuntimeException("Input Mb range is invalid");
		this.lowerMb = lowerMb;
		this.upperMb = upperMb;
	}

	/**
	 * Set Ms range
	 * 
	 * @param lowerMs
	 *            lower limit of Ms
	 * @param upperMs
	 *            upper limit of Ms
	 */
	public void setMsRange(double lowerMs, double upperMs) {
		if (upperMs < lowerMs)
			throw new RuntimeException("input Ms range is invalid");
		this.lowerMs = lowerMs;
		this.upperMs = upperMs;
	}

	/**
	 * Set Mw Range
	 * 
	 * @param lowerMw
	 *            lower limit of Mw range
	 * @param upperMw
	 *            upper limit of Mw range
	 */
	public void setMwRange(double lowerMw, double upperMw) {
		if (upperMw < lowerMw)
			throw new RuntimeException("input Mw range is invalid");
		this.lowerMw = lowerMw;
		this.upperMw = upperMw;
	}

	/**
	 * Set tension axis range [0:90]
	 * 
	 * @param lowerNullAxisPlunge
	 *            lower limit of Null axis plunge
	 * @param upperNullAxisPlunge
	 *            upper limit of Null axis plunge
	 */
	public void setNullAxisPlungeRange(int lowerNullAxisPlunge, int upperNullAxisPlunge) {
		if (upperNullAxisPlunge < lowerNullAxisPlunge || 90 < upperNullAxisPlunge || lowerNullAxisPlunge < 0)
			throw new RuntimeException("input null axis plunge range is invalid");
		this.lowerNullAxisPlunge = lowerNullAxisPlunge;
		this.upperNullAxisPlunge = upperNullAxisPlunge;

	}

	/**
	 * CMT time must be after the startTime
	 * 
	 * @param startTime
	 *            start time of search range
	 */
	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	/**
	 * Set tension axis range [0:90]
	 * 
	 * @param lowerTensionAxisPlunge
	 *            [deg] lower limit of tension axis plunge
	 * @param upperTensionAxisPlunge
	 *            [deg] upper limit of tension axis plunge
	 */
	public void setTensionAxisPlungeRange(int lowerTensionAxisPlunge, int upperTensionAxisPlunge) {
		if (lowerTensionAxisPlunge < 0 || upperTensionAxisPlunge < lowerTensionAxisPlunge
				|| 90 < upperTensionAxisPlunge)
			throw new RuntimeException("invalid tension axis plunge range");
		this.lowerTensionAxisPlunge = lowerTensionAxisPlunge;
		this.upperTensionAxisPlunge = upperTensionAxisPlunge;
	}

	public void setUpperNullAxisPlunge(int upperNullAxisPlunge) {
		this.upperNullAxisPlunge = upperNullAxisPlunge;
	}

	public void setUpperTensionAxisPlunge(int upperTensionAxisPlunge) {
		this.upperTensionAxisPlunge = upperTensionAxisPlunge;
	}

}