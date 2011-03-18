import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import uxparser.XParser;

public class GeocoodingInfo {

    /**
     * アドレス
     */
    private String address;

    /**
     * 緯度
     */
    private double lat;

    /**
     * 経度
     */
    private double lng;

    // private Vector choices = new Vector();

    // private String errorCode;
    //
    // private int status;
    //
    // public static final int STATUS_SUCCESS = 0;
    //
    // public static final int STATUS_MULTI = 1;
    //
    // public static final int STATUS_FAILURE = 2;

    private static GeocoodingInfo getInstance() {
        return new GeocoodingInfo();

    }

    private static String urlEncode(String str) {
        StringBuffer buffer = new StringBuffer();

        try {
            byte[] b = str.getBytes("UTF-8");

            for (int i = 0; i < b.length; ++i) {

                String hexString = Integer.toHexString(b[i]);
                hexString = hexString.substring(hexString.length() - 2);
                buffer.append("%" + hexString);

            }
        } catch (UnsupportedEncodingException e) {

        }
        return buffer.toString();

    }

    public static Vector getGeocordingInfo(String str, int motoData) {
        // String url = "http://www.geocoding.jp/api/?q=" + urlEncode(str);
        String url = "http://geocode.csis.u-tokyo.ac.jp/cgi-bin/simple_geocode.cgi?geosys=tokyo&charset=UTF8&addr="
                + urlEncode(str) + "&series=";
        switch (motoData)
            {
            case 0:
                url += "PLACE";
                break;
            case 1:
                url += "STATION";
                break;
            case 2:
                url += "ADDRESS";
                break;
            default:
                throw new IllegalArgumentException("motoData: " + motoData);
            }

        try {
            // System.err.println(url);
            byte[] b = HttpUtil.getBytesViaHttp(url);
            return parseGeocoodingResponse(new ByteArrayInputStream(b));
        } catch (Exception e) {
        }
        return null;

    }

    private static Vector parseGeocoodingResponse(InputStream is) {
        // GeocoodingInfo info = GeocoodingInfo.getInstance();
        Vector vector = new Vector();
        XParser parser = null;

        try {
            parser = XParser.make(is);

            int code;
            GeocoodingInfo info = null;
            while ((code = parser.next()) != XParser.EOF) {

                switch (code)
                    {
                    case XParser.ELEM_START:
                        if (parser.elem().name().equals("candidate")) {
                            info = GeocoodingInfo.getInstance();
                            vector.addElement(info);
                        }
                        break;
                    case XParser.TEXT:
                        if (parser.elem().name().equals("latitude")) {
                            if (info != null) {
                                info.setLat(Double.parseDouble(parser.text()
                                        .string()));
                            }
                        } else if (parser.elem().name().equals("longitude")) {
                            if (info != null) {
                                info.setLng(Double.parseDouble(parser.text()
                                        .string()));
                            }
                        } else if (parser.elem().name().equals("address")) {
                            if (info != null) {
                                info.setAddress(parser.text().string());
                            }
                        }

                        break;
                    }

                /*
                 * switch (code) { case XParser.ELEM_START: if
                 * (parser.elem().name().equals("error")) {
                 * info.setStatus(GeocoodingInfo.STATUS_FAILURE); } else if
                 * (parser.elem().name().equals("coordinate")) {
                 * info.setStatus(GeocoodingInfo.STATUS_SUCCESS); } else if
                 * (parser.elem().name().equals("choices")) {
                 * info.setStatus(GeocoodingInfo.STATUS_MULTI); }
                 * 
                 * break; case XParser.TEXT: if
                 * (parser.elem().name().equals("error")) {
                 * info.setErrorCode(parser.text().string()); return info; }
                 * else if (parser.elem().name().equals("lat")) {
                 * info.setLat(Double.parseDouble(parser.text().string())); }
                 * else if (parser.elem().name().equals("lng")) {
                 * info.setLng(Double.parseDouble(parser.text().string())); }
                 * else if (parser.elem().name().equals("choice")) {
                 * info.addChoice(parser.text().string()); } else if
                 * (parser.elem().name().equals("address")) {
                 * info.setAddress(parser.text().string()); }
                 * 
                 * 
                 * break; case XParser.ELEM_END: break; default: }
                 */
            }
        } catch (Exception e) {
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
        return vector;
    }

    /**
     * @param status
     */
    private GeocoodingInfo() {
        super();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // public Vector getChoices() {
    // return choices;
    // }
    //
    // public void addChoice(String choice) {
    // choices.addElement(choice);
    // }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    //
    // public int getStatus() {
    // return status;
    // }
    //
    // public String getErrorCode() {
    // return errorCode;
    // }
    //
    // public void setErrorCode(String errorCode) {
    // this.errorCode = errorCode;
    // }
    //
    // public void setStatus(int status) {
    // this.status = status;
    // }

    /*
     * 緯度 -444.952 * x + 67393.6 a = -2.73186 +/- 0.0324 (1.186%) b = -256.392
     * +/- 2.244 (0.8753%) c = 64237.9 経度 364.101 * x + 65534.1 a = -0.00183205
     * +/- 0.001152 (62.86%) b = 364.594 +/- 0.3099 (0.085%) c = 65501 +/- 20.82
     * (0.03178%)
     * 
     * 
     */

    /*
     * 緯度 y = sigma Ai * x^i
     * 
     * A0 = 65075.8820342895 A1 = -306.840892815428 A2 = -2.74975444254558 A3 =
     * 0.0438525983751735 A4 = -0.0006206473704376
     * 
     * Corr. Coeff. = 0.999999992511341
     * 
     * 経度
     * 
     * y = sigma Ai * x^i
     * 
     * A0 = 65490.1380675011 A1 = 364.742571409815 A2 = -0.00234809008218882
     * 
     * Corr. Coeff. = 0.999999991228484
     * 
     * 
     */
    /*
     * http://homepage3.nifty.com/Nowral/02_DATUM/02_DATUM.html#HowTo によって測地系を変換
     * BWGS84 = BTokyo - 0.00010695*BTokyo + 0.000017464*LTokyo + 0.0046017
     * LWGS84 = LTokyo - 0.000046038*BTokyo - 0.000083043*LTokyo + 0.010040
     */
    public int getMapX() {
        double lng = this.lng - 0.000046038 * this.lat - 0.000083043 * this.lng
                + 0.010040;

        return (int) (65490.1380675011 + 364.742571409815 * lng + (-0.00234809008218882)
                * lng * lng);
    }

    public int getMapY() {
        double lat = this.lat - 0.00010695 * this.lat + 0.000017464 * this.lng
                + 0.0046017;

        return (int) (65075.8820342895 + (-306.840892815428) * lat
                + (-2.74975444254558) * lat * lat + (0.0438525983751735) * lat
                * lat * lat + (-0.0006206473704376) * lat * lat * lat * lat);
    }

}
