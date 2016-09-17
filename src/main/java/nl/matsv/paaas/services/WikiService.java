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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
// TODO cache and create a task to refresh every x hours/minutes
public class WikiService {
    private final WikiExtractor wikiExtractor;
    private Map<Integer, WikiData> map = new ConcurrentHashMap<>();

    @Autowired
    public WikiService(WikiExtractor wikiExtractor) {
        this.wikiExtractor = wikiExtractor;
        try {
            refreshData();
        } catch (IOException e) {
            System.out.println("Failed to download the wiki data");
        }
    }

    public void refreshData() throws IOException {
        this.map = wikiExtractor.extractWiki();
    }

    public Optional<WikiData> getWikiData(int protocol) {
        if (map.containsKey(protocol))
            return Optional.ofNullable(map.get(protocol));
        return Optional.empty();
    }
}
