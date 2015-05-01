import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fw.ccg.util.CCGUtility;
import com.fw.ccg.xml.DOMFormatter;
import com.fw.ccg.xml.XMLUtil;
import com.fw.dao.qry.DefaultQueryFunctions;


public class Test
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		TransformerFactory transfabrik  = TransformerFactory.newInstance(); 
		Transformer        sTransformer = transfabrik.newTransformer(); 
		 
		sTransformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
		sTransformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4"); 
		 
		// Fill Xml elements
		DocumentBuilderFactory fabrik   = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder        sBuilder = fabrik.newDocumentBuilder(); 
		 
		Document sDokument = sBuilder.newDocument(); 
		Element dokuElement = (Element) sDokument.createElement("root"); 
		sDokument.appendChild(dokuElement); 
		 
		Element someElement = sDokument.createElement("Mainelement"); 
		someElement.setAttribute("Name", "Myname-" + 100); 
		 
		Element anotherElmement = sDokument.createElement("Subelement"); 
		anotherElmement.setAttribute("Bla", "60"); 
		someElement.appendChild(anotherElmement); 
		// etc 
		dokuElement.appendChild(someElement); 
		
		XMLUtil.addTextNode("txt","This is test data \ndasdfs",anotherElmement,sDokument);
		 
		// Write file
		DOMSource    domsource = new DOMSource(sDokument); 
		StreamResult output    = new StreamResult(System.out); // new File("File.xml")); 
		sTransformer.transform(domsource, output);
		
		System.out.println("\n\n\n\n");
		System.out.println(new DOMFormatter().toString(dokuElement));
	}

}
