import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.net.ssl.*;
import javax.persistence.Query;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main implements Runnable{
    private Thread t;
    private String threadName;

    Main(String name) {
        threadName = name;
        System.out.println("Creating thread: " + name);
    }

    public static void main(String[] args) {
        try {
            SSLCertificate();
            Main r1 = new Main("Telegram bot");
            r1.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void SSLCertificate() throws NoSuchAlgorithmException, KeyManagementException {
        /* Start of Fix */
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        /* End of the fix*/
    }

    public static void HTTPSendPhoto(String imageDirectory) throws IOException {
        String url = "https://api.telegram.org/bot5364421343:AAEATRGZud2KIicgvWRxBW31OURN07erqb8/sendPhoto?";
        String charset = "UTF-8";
        String chat_id = "5176390876";
        String caption = "Select data from bccs.im";
        File imageFile = new File(imageDirectory);
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        URLConnection connection = new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


        try (
                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
        ) {
            // Send normal chat Id .
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"chat_id\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
            writer.append(CRLF).append(chat_id).append(CRLF).flush();

            // Send normal caption.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"caption\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
            writer.append(CRLF).append(caption).append(CRLF).flush();

            // Send file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"" + imageFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: image/jpeg; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
            writer.append(CRLF).flush();
            Files.copy(imageFile.toPath(), output);
            output.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();
        }
        // Request is lazily fired whenever you need to obtain information about response.
        int responseCode = ((HttpURLConnection) connection).getResponseCode();
        System.out.println("================= HTTP ====================");
        System.out.println("Status: " + responseCode); // Should be 200
    }

    @Override
    public void run() {
        System.out.println("Running " + threadName);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH.mm.ss");
            while (true) {
                System.out.println("Thread: " + threadName + " on " + sdf.format(new Date()));
                String fileName = ChartReport();
                System.out.println(fileName);
                HTTPSendPhoto(fileName.toString());
                Thread.sleep(60000);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + threadName + " interrupted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public String ChartReport() throws IOException {

        final CategoryDataset []dataset = createDataset();
        final org.jfree.chart.axis.NumberAxis rangeAxis1 = new org.jfree.chart.axis.NumberAxis("Value of amount not tax");
        rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        final LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
        renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

        final CategoryPlot subplot1 = new CategoryPlot(dataset[0], null, rangeAxis1, renderer1);
        subplot1.setDomainGridlinesVisible(true);

        final NumberAxis rangeAxis2 = new NumberAxis("Value of amount tax");
        rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        final BarRenderer renderer2 = new BarRenderer();
        renderer2.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

        final CategoryPlot subplot2 = new CategoryPlot(dataset[1], null, rangeAxis2, renderer2);
        subplot2.setDomainGridlinesVisible(true);

        final CategoryAxis domainAxis = new CategoryAxis("Category");
        final CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
        plot.add(subplot1, 1);
        plot.add(subplot2, 1);

        final JFreeChart result = new JFreeChart(
                "Combined sale trans",
                new Font("SansSerif", Font.BOLD, 12),
                plot,
                true
        );

        SimpleDateFormat simpleFormatter = new SimpleDateFormat("dd-MM-yyyy-HH.mm.ss");
        String date = simpleFormatter.format(new Date());
        String fileName = "ChartReport_" + date + ".jpg";
        File ChartReport = new File(fileName);
        ChartUtilities.saveChartAsJPEG(ChartReport, result, 1280, 920);
        System.out.println("Image name: " + ChartReport.getAbsolutePath());
        return ChartReport.getAbsolutePath();

    }
    private DefaultCategoryDataset[] createDataset() {
        StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml")
                .build();
        Metadata meta = new MetadataSources(ssr).getMetadataBuilder().build();
        SessionFactory factory = meta.getSessionFactoryBuilder().build();
        Session session = factory.openSession();
        Transaction transaction = session.beginTransaction();

        java.util.List<Object[]> lstResult1 = new ArrayList<>();
        java.util.List<Object[]> lstResult2 = new ArrayList<>();
        DefaultCategoryDataset[] str = new DefaultCategoryDataset[2];
        try {

            Query query1 = session.createNativeQuery("SELECT SUM(AMOUNT_NOT_TAX), to_char(SALE_TRANS_DATE,'dd/mm') FROM BCCS_IM.SALE_TRANS WHERE SALE_TRANS_TYPE = '89' GROUP BY SALE_TRANS_DATE ORDER BY SALE_TRANS_DATE ASC");
            lstResult1 = query1.getResultList();

            Query query2 = session.createNativeQuery("SELECT SUM(AMOUNT_TAX), to_char(SALE_TRANS_DATE,'dd/mm') FROM BCCS_IM.SALE_TRANS WHERE SALE_TRANS_TYPE = '89' GROUP BY SALE_TRANS_DATE ORDER BY SALE_TRANS_DATE ASC");
            lstResult2 = query2.getResultList();

            DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
            String series1 = "Amount not tax";

            int count = 0;
            for (Object[] data1 : lstResult1) {
                System.out.println("===== Here is value of data 1 selected =====");
                BigDecimal sum = (BigDecimal) data1[0];
                double num1 = sum.doubleValue();
                String date1 = (String) data1[1];
                System.out.println("Sum : " + sum + " Date: " + date1);
                dataset1.addValue(num1, series1, date1);

                count++;
                if (count == 14) break;

            }

            DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
            String series2 = "Amount tax";
            count = 0;
            for (Object[] data2 : lstResult2) {
                System.out.println("==== Here is value data 2 selected ====");
                BigDecimal sum2 = (BigDecimal) data2[0];
                double num2 = sum2.doubleValue();
                String date2 = (String) data2[1];
                System.out.println("Sum : " + sum2 + " Date: " + date2);
                dataset2.addValue(num2, series2, date2);

                count++;
                if (count == 14) break;
            }
            str[0] = dataset1;
            str[1] = dataset2;

            return str;

        } catch (Exception ex) {
            ex.printStackTrace();
            if (transaction.isActive()) {
                transaction.rollback();
            }
            session.close();
            factory.close();
        }
        return null;
    }
//    private DefaultCategoryDataset createDataset2() {
//
//        StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
//                .configure("hibernate.cfg.xml")
//                .build();
//        Metadata meta = new MetadataSources(ssr).getMetadataBuilder().build();
//        SessionFactory factory = meta.getSessionFactoryBuilder().build();
//        Session session = factory.openSession();
//        Transaction transaction = session.beginTransaction();
//
//
//        List<Object[]> lstResult2 = new ArrayList<>();
//
//
//        try {
//
//            Query query2 = session.createNativeQuery("SELECT SUM(AMOUNT_TAX), to_char(SALE_TRANS_DATE,'dd/mm') FROM BCCS_IM.SALE_TRANS WHERE SALE_TRANS_TYPE = '89' GROUP BY SALE_TRANS_DATE ORDER BY SALE_TRANS_DATE ASC");
//
//
//            lstResult2 = query2.getResultList();
//
//            DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
//            String series2 = "Amount tax";
//            int count = 0;
//            for (Object[] data : lstResult2) {
//            System.out.println("==== Here is value data 2 selected ====");
//            BigDecimal sum2 = (BigDecimal) data[0];
//            double num2 = sum2.doubleValue();
//            String date2 = (String) data[1];
//            System.out.println("Sum : " + sum2 + " Date: " + date2);
//            dataset2.addValue(num2, series2, date2);
//
//            count++;
//            if (count == 14) break;
//
//
//    }
//            return dataset2;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }
//            session.close();
//            factory.close();
//        }
//        return null;
//    }

}