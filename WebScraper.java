package webScraper2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

 class WebScraper {
    public static void main(String[] args) {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            String url = "https://bbc.com";
            Document doc = Jsoup.connect(url).get();

            Elements articleLinks = doc.select("a[href*='/news/']");

            for (Element link : articleLinks) {
                String headline = link.text();
                String articleUrl = link.absUrl("href");

                if (!articleUrl.isEmpty()) {
                    try {
                        Document articleDoc = Jsoup.connect(articleUrl).get();

                        String publicationDate = "N/A";
                        Element timeTag = articleDoc.selectFirst("time");
                        if (timeTag != null && timeTag.hasAttr("datetime")) {
                            publicationDate = timeTag.attr("datetime");
                        }

                        String author = "N/A";
                        Element authorTag = articleDoc.selectFirst(".byline__name, .ssrcss-68pt20-Contributor");
                        if (authorTag != null) {
                            author = authorTag.text();
                        }

                        articles.add(new NewsArticle(headline, publicationDate, author));

                    } catch (IOException e) {
                        System.out.println("Skipping failed article: " + articleUrl);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error fetching the BBC homepage:");
            e.printStackTrace();
        }

        // Print all collected articles
        System.out.println("Scraped Articles:\n");
        for (NewsArticle article : articles) {
            System.out.println(article);
        }
    }
}
