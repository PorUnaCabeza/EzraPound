package util.epub.epub;

import java.io.OutputStream;

import util.epub.domain.Resource;

public interface HtmlProcessor {
	
	void processHtmlResource(Resource resource, OutputStream out);
}
