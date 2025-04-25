package Servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

@WebServlet("/scrape")
public class ScrapeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //  SESSION TRACKING 
        HttpSession session = request.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) visitCount = 0;
        session.setAttribute("visitCount", ++visitCount);
       

        String url = request.getParameter("url");
        String[] options = request.getParameterValues("options");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        List<ScrapedData> scrapedDataList = new ArrayList<>();

        out.println("<html><head><title>Scraping Results</title>");
        out.println("<style>table {border-collapse: collapse;} th, td {border: 1px solid #ccc; padding: 10px;}</style>");
        out.println("</head><body>");
        out.println("<h2>Scraping Results</h2>");

        //  Display visit count
        out.println("<p style='color: blue;'>You have visited this page " + visitCount + " times.</p>");

        if (url == null || url.isEmpty()) {
            out.println("<p style='color:red;'>No URL provided.</p></body></html>");
            return;
        }

        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            Document doc = Jsoup.connect(url).get();

            out.println("<table id='resultsTable'><tr><th>Type</th><th>Content</th></tr>");

            if (options != null) {
                for (String option : options) {
                    switch (option) {
                        case "title":
                            String title = doc.title();
                            scrapedDataList.add(new ScrapedData("Title", title));
                            out.println("<tr><td>Title</td><td>" + title + "</td></tr>");
                            break;

                        case "links":
                            Elements links = doc.select("a[href]");
                            for (Element link : links) {
                                String linkText = link.text();
                                String linkHref = link.absUrl("href");
                                scrapedDataList.add(new ScrapedData("Link", linkText + " (" + linkHref + ")"));
                                out.println("<tr><td>Link</td><td><a href='" + linkHref + "'>" + linkText + "</a></td></tr>");
                            }
                            break;

                        case "images":
                            Elements images = doc.select("img[src]");
                            for (Element img : images) {
                                String imgSrc = img.absUrl("src");
                                scrapedDataList.add(new ScrapedData("Image", imgSrc));
                                out.println("<tr><td>Image</td><td><img src='" + imgSrc + "' width='100'></td></tr>");
                            }
                            break;
                    }
                }
            } else {
                out.println("<tr><td colspan='2'>No options selected.</td></tr>");
            }

            out.println("</table><br>");

            //  JSON Conversion
            Gson gson = new Gson();
            String json = gson.toJson(scrapedDataList);

            out.println("<h3>JSON Output</h3>");
            out.println("<textarea rows='10' cols='100'>" + json + "</textarea><br><br>");

            //  CSV Button Script
            out.println("<button onclick='downloadCSV()'>Download as CSV</button>");
            out.println("<script>");
            out.println("function downloadCSV() {");
            out.println("  let rows = document.querySelectorAll('table#resultsTable tr');");
            out.println("  let csv = [];");
            out.println("  for (let row of rows) {");
            out.println("    let cols = row.querySelectorAll('td, th');");
            out.println("    let rowData = Array.from(cols).map(td => '\"' + td.innerText.replace(/\"/g, '\"\"') + '\"');");
            out.println("    csv.push(rowData.join(','));");
            out.println("  }");
            out.println("  let blob = new Blob([csv.join('\\n')], { type: 'text/csv' });");
            out.println("  let a = document.createElement('a');");
            out.println("  a.href = URL.createObjectURL(blob);");
            out.println("  a.download = 'scraped_data.csv';");
            out.println("  a.click();");
            out.println("}");
            out.println("</script>");

        } catch (Exception e) {
            out.println("<p style='color:red;'>Error: " + e.getMessage() + "</p>");
        }

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
