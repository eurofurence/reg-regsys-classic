package org.eurofurence.regsys.service.camtv8;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.List;

public class Parser {
    public static List<Entry> parse(InputStream rawXml) {
        try {
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);

            final XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(rawXml);

            JAXBContext ctx = JAXBContext.newInstance(Camtv8Document.class);

            Unmarshaller um = ctx.createUnmarshaller();

            Camtv8Document document = (Camtv8Document) um.unmarshal(xmlStreamReader);

            return document.getNtryList().stream()
                    .filter(Entry::filterValid)
                    .map(Entry::fromNtry)
                    .filter(e -> e.amount > 0)
                    .toList();

        } catch (JAXBException | XMLStreamException e) {
            throw new RuntimeException("error parsing CAMTv8 052 or 053 XML", e);
        }
    }
}
