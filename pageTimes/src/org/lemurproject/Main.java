package org.lemurproject;

import net.htmlparser.jericho.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jwat.common.Uri;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    public static ArrayList<String> linksOnPage(Source source) {
        ArrayList<String> links = new ArrayList<String>();
        List<Element> linkElements = source.getAllElements(HTMLElementName.A);

        for (Element element : linkElements) {
            links.add(element.getAttributeValue("href"));
        }

        return links;
    }

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("prog")
                .description("Process some integers.");
        parser.addArgument("warcFilesList")
                .metavar("warc-files-list")
                .help("Text file containing a list of warc files");
        parser.addArgument("--vbulletin")
                .dest("vbulletin")
                .action(Arguments.storeTrue())
                .help("Process vbulletin");
        parser.addArgument("--phpbb")
                .dest("phpbb")
                .action(Arguments.storeTrue())
                .help("Process phpbb");
        parser.addArgument("--yuku")
                .dest("yuku")
                .action(Arguments.storeTrue())
                .help("Process yuku");

        Namespace res;
        boolean vbulletin = false;
        boolean phpbb = false;
        boolean yuku = false;
        try {
            res = parser.parseArgs(args);
            vbulletin = res.getBoolean("vbulletin");
            phpbb = res.getBoolean("phpbb");
            yuku = res.getBoolean("yuku");

        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }


        Config.LoggerProvider = LoggerProvider.DISABLED;
        String warcFilesList = args[0];

        BufferedReader reader = new BufferedReader(new FileReader(warcFilesList));

        String currentWarcFile;

        while ((currentWarcFile = reader.readLine()) != null) {
            WarcReader warcReader = WarcReaderFactory.getReader(new FileInputStream(currentWarcFile));

            WarcRecord record;

            while ((record = warcReader.getNextRecord()) != null) {

                if (!(record.header.contentTypeStr.matches(".*response"))) {
                    continue;
                }

                Source source = new Source(record.getPayloadContent());
                TextExtractor textExtractor = new TextExtractor(source);

                ArrayList<Date> dates = PageTimes.datesInText(textExtractor.toString());

                boolean success = false;

                for (Date date : dates) {
                    success = success || (PageTimes.inTimeRange(date));
                }

                if (success) {
                    ArrayList<String> links = linksOnPage(source);

                    for (String link : links) {

                        String pattern = ".*";

                        if (yuku) {
                            pattern = Constants.yukuTopicLinks;
                        } else if (vbulletin) {
                            pattern = Constants.vbulletinTopicLinks;
                        } else if (phpbb) {
                            pattern = Constants.phpbbTopicLinks;
                        }

                        if (link != null && link.matches(pattern)) {
                            try{
                                Uri uri = new Uri(link);
                                if (uri.isAbsolute()) {
                                    System.out.println(link);
                                }
                                else {
                                    URI warcUri = new URI(record.header.warcTargetUriStr);
                                    System.out.println(warcUri.resolve(new URI(link)).toString());
                                }
                            }
                            catch (Exception e) {

                            }
                        }
                    }
                }
            }
        }

    }

}
