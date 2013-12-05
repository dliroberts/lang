package grammar.input.xml;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import java.io.CharConversionException;

public class DataParser<D> {
	private Class<DataHandler<D>> handler;
	
	public DataParser(Class<DataHandler<D>> handler) {
		this.handler = handler;
	}

	public D parse(File xmlFile) throws IOException, IllegalLoadOrderException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			DataHandler<D> handlerInst = handler.newInstance();
			parser.parse(xmlFile, handlerInst);
			
			return handlerInst.getParsedData();
		}
		catch (CharConversionException mbse) {
			throw new IOException("Please save file '"+
					xmlFile.getAbsolutePath()+"' using UTF-8 encoding.");
		}
		catch (SAXException se) {
			throw new IOException(se);
		}
		catch (ParserConfigurationException pce) {
			throw new IOException(pce);
		} catch (InstantiationException e) {
			throw new Error("Unable to instantiate class "+handler.getName()+
					": "+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new Error("Unable to instantiate class "+handler.getName()+
					": "+e.getMessage());
		}
	}
}