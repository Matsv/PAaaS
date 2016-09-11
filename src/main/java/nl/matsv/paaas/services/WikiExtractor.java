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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class WikiExtractor {

    /**
     * Get all the versions that link to an official Protocol page
     *
     * @return A map with the protocol ID + edit url
     * @throws IOException
     */
    public Map<Integer, String> getVersions() throws IOException {
        Document doc = Jsoup.connect("http://wiki.vg/Protocol_version_numbers").get();
        Element table = doc.select(".wikitable").get(0);
        Element body = table.select("tbody").get(0);

        Map<Integer, String> maps = new HashMap<>();

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
                            // TODO handle snapshots
                            if (!(url.get().toLowerCase().contains("title=protocol") || url.get().toLowerCase().contains("vg/protocol")))
                                url = Optional.empty();
                            else
                                url = getEditUrl(url.get());
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

    // TODO Extract packet names
    public Map<String, Map<String, Map<Integer, String>>> getPacketNames(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        return null;
    }

    protected Optional<String> getEditUrl(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Element el;
        if ((el = doc.getElementById("#ca-edit")) != null || (el = doc.getElementById("ca-viewsource")) != null) {
            Optional<Element> opa = Optional.ofNullable(el.select("a").first());
            if (!opa.isPresent())
                return Optional.empty();
            return Optional.of(opa.get().attr("abs:href"));
        } else {
            System.out.println("Could not find any source/edit for wiki page: " + url);
            return Optional.empty();
        }
    }
}
