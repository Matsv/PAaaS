/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaas.services;

import nl.matsv.paaas.data.wiki.WikiData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.python.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WikiExtractor {

    public Map<Integer, WikiData> extractWiki() throws IOException {
        Map<Integer, String> versions = getVersions();

        Map<Integer, WikiData> data = new ConcurrentHashMap<>();

        for (Map.Entry<Integer, String> version : versions.entrySet()) {
            try {
                data.put(version.getKey(), new WikiData(version.getValue(), getPacketNames(version.getValue())));
                System.out.println("Extracted protocol " + version.getKey() + " from wiki.vg");
            } catch (Exception e) {
                System.out.println("Failed to download wiki data for version " + version.getKey() + " (" + version.getValue() + ")");
                e.printStackTrace();
            }
        }

        return data;
    }

    /**
     * Get all the versions that link to an official Protocol page
     *
     * @return A map with the protocol ID + edit url
     * @throws IOException
     */
    protected Map<Integer, String> getVersions() throws IOException {
        Document doc = Jsoup.connect("http://wiki.vg/Protocol_version_numbers").get();
        Element table = doc.select(".wikitable").get(0);
        Element body = table.select("tbody").get(0);

        Map<Integer, String> maps = new TreeMap<>();

        for (Element tr : body.select("tr")) {
            Elements tds = tr.select("td");
            if (tds.size() != 3) continue;

            int pid = -1;
            Optional<String> url = Optional.empty();
            for (int i = 0; i < tds.size(); i++) {
                Element td = tds.get(i);

                // Ignore version name for now TODO check rowspan
                if (i == 0)
                    continue;
                if (i == 1)
                    pid = Integer.parseInt(td.text());
                else {
                    Optional<Element> opa = Optional.ofNullable(td.select("a").first());
                    if (!opa.isPresent()) {
                        pid = -1;
                        break;
                    } else {
                        url = Optional.ofNullable(opa.get().attr("href"));
                        if (url.isPresent()) {
                            // The current protocol will directly link to /protocol TODO check if there is a better way to do this with jsoup
                            if (url.get().toLowerCase().equals("/protocol"))
                                url = Optional.of("http://wiki.vg/Protocol");
                            // TODO handle snapshots
                            if (!(url.get().toLowerCase().contains("title=protocol") ||
                                    url.get().toLowerCase().contains("vg/protocol")))
                                url = Optional.empty();
                        }
                    }
                }
            }

            if (pid != -1 && url.isPresent()) {
                maps.put(pid, url.get());
            }
        }
        return maps;
    }

    /**
     * Extract packet names from the protocol page
     *
     * @param url wiki url
     * @return A way too complex map (good luck :))
     * @throws Exception Panic
     */
    protected Map<String, Map<String, Map<Integer, String>>> getPacketNames(String url) throws Exception {
        Map<String, Map<String, Map<Integer, String>>> dataMap = new TreeMap<>();
        Document doc = Jsoup.connect(url).get();
        Elements wikiTables = doc.select(".wikitable");

        for (Element el : wikiTables) {
            Element tBody = el.select("tbody").get(0);
            Elements trs = tBody.select("tr");

            if (!isPacket(trs.get(0)))
                continue;

            Element tr = trs.get(1);
            Elements tds = tr.select("td");

            String packetName = findPacketName(el);
            int id = Integer.parseInt(tds.get(0).text().substring(2), 16); // Remove 0x
            String state = tds.get(1).text().toLowerCase();
            String bounding = tds.get(2).text().toLowerCase() + "bound";

            if (!dataMap.containsKey(state))
                dataMap.put(state, Maps.newTreeMap());
            if (!dataMap.get(state).containsKey(bounding))
                dataMap.get(state).put(bounding, Maps.newTreeMap());
            dataMap.get(state).get(bounding).put(id, packetName);
        }

        return dataMap;
    }

    private String findPacketName(Element el) throws Exception {
        Element previous = el.previousElementSibling();

        // At the moment never more than 15 (Blame 1.7.10 :'()
        for (int i = 0; i < 15; i++) {
            if (hasAttr(previous, "span") && previous.select("span").first().classNames().contains("mw-headline")) {
                return toSnakeCase(getPacketName(previous));
            }
            previous = previous.previousElementSibling();
        }

        throw new Exception("No packet name found for " + el);
    }

    private String getPacketName(Element el) {
        return el.select("span").first().text().toLowerCase();
    }

    // TODO Find a way inside jsoup to do this correctly
    private boolean hasAttr(Element el, String attr) {
        return Optional.ofNullable(el.select(attr).first()).isPresent();
    }

    private boolean isPacket(Element header) {
        Elements ths = header.select("th");
        return ths.size() >= 3 &&
                ths.get(0).text().equalsIgnoreCase("Packet ID") &&
                (ths.get(1).text().equalsIgnoreCase("State") || ths.get(1).text().equalsIgnoreCase("Category")) &&
                ths.get(2).text().equalsIgnoreCase("Bound To");
    }

    private String toSnakeCase(String text) {
        return String.join("_", text.split(" "));
    }
}
