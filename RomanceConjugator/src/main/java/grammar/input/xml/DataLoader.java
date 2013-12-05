package grammar.input.xml;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataLoader<D> {
	private final List<D> loadedData = new ArrayList<D>();
	
	private final File xmlFolder;
	private final String filenameSuffix;
	private final DataParser<D> parser;
	
	public DataLoader(File xmlFolder, String filenameSuffix, DataHandler<D> handler) {
		this.xmlFolder = xmlFolder;
		this.filenameSuffix = filenameSuffix;
		Class<DataHandler<D>> handlerClass = (Class<DataHandler<D>>) handler.getClass();
		parser = new DataParser<D>(handlerClass);
	}
	
	public D loadDataItem() throws IOException {
		return loadData().get(0); // TODO: implement properly
	}
	
	public List<D> loadData() throws IOException {
		if (!xmlFolder.exists())
			return new ArrayList<D>(); // nothing to load
		
		List<File> xmlFiles = Arrays.asList(xmlFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				 return name.endsWith(filenameSuffix);
			}
		}));
		
		int nFailed = -1;
		List<File> failedToLoadData;
		do {
			failedToLoadData = new ArrayList<File>();
			for (File xmlFile : xmlFiles) {
				try {
					loadedData.add(parser.parse(xmlFile));
				}
				catch (IllegalLoadOrderException iloe) {
					failedToLoadData.add(xmlFile);
				}
				catch (Exception e) {
					throw new IOException("Unable to load "+xmlFile+"; error during parsing.",e);
				}
			}
			xmlFiles = failedToLoadData;
			if (failedToLoadData.size() == nFailed)
				throw new IllegalStateException("Did not load more data from "+xmlFolder.getAbsolutePath()+
						" in this iteration. Remaining files: "+failedToLoadData.toString());
			nFailed = failedToLoadData.size();
		} while (xmlFiles.size() != 0);
		
		return loadedData;
	}
}