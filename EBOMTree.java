package com.yura.draw.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Class Name : EBOMTree.java
 * @Description : BOM 트리구조 xml 파싱을 위한 클래스
 * @Modification Information
 *
 *    수정일       수정자         수정내용
 *    -------        -------     -------------------
 *    2019. 3. 21.     이하얀
 *
 * @author lhy70901
 * @since 2019. 3. 21.
 * @version 1.0
 * @see
 *
 */
public class EBOMTree {
	public Map<String, Object> xmlParsing(String result_file) throws Exception{
		List<Map<String, Object>> EBOM = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> tmpLinkID = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> resLinkID = null;
		Map<String, Object> ret_EBOM_LinkID = new HashMap<String, Object>();
		
		// XML 문서 파싱
		File xmlFile = new File(result_file);				
		DocumentBuilderFactory xFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xBuilder = xFactory.newDocumentBuilder();
		Document doc = xBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		
		
		// arrayList EBOM
		NodeList root_nList = doc.getElementsByTagName("Root");		
		Node root_Node = root_nList.item(0);
		Element root_ele = (Element)root_Node;
		
		if (root_Node.hasChildNodes()) {
			recurTreeChild(root_Node, EBOM, tmpLinkID);
		}
		
		
		// hashMap fileName
		Element root = doc.getDocumentElement();
		NodeList childNode = root.getChildNodes();
		boolean first = true;		
		HashMap<String, String> fileName = new HashMap<String, String>();
		for (int idx = 0; idx < childNode.getLength(); idx++) {
			Node node = childNode.item(idx);
			if (node.getNodeType() == Node.ELEMENT_NODE) {	// 해당 노드의 종류 판정(ELEMENT 일때)
				Element ele = (Element)node;
				String nodeName = ele.getNodeName();
				
				if (first){
					Map<String, Object> tmp = new HashMap<String, Object>();
					first = false;
					fileName.put(root_ele.getAttribute("Identifier"), ele.getAttribute("SourceUuid"));
					tmp.put("identifier", root_ele.getAttribute("Identifier"));
					tmp.put("reflink", root_ele.getAttribute("Identifier"));
					tmpLinkID.add(tmp);
				} else {
					fileName.put(ele.getAttribute("Identifier"), ele.getAttribute("SourceUuid"));
				}
			}
		}
		
		// LinkID <identifier, reflink>  ->  LinkID <identifier, reflink, filename>
		for (int idx2 = 0; idx2 < tmpLinkID.size(); idx2++) {
			String tmp = String.valueOf(tmpLinkID.get(idx2).get("reflink"));
			for (String key : fileName.keySet()) {				
				if (tmp.equals(key)) {
					tmpLinkID.get(idx2).put("filename", fileName.get(key));
				}
			}
		}
		
		// 중복 제거
		resLinkID = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(tmpLinkID));
		
		
		// EBOM id_tree  ->  filename_tree
		for (int idx3 = 0; idx3 < EBOM.size(); idx3++) {
			String cur_tmp = String.valueOf(EBOM.get(idx3).get("current"));
			String par_tmp = String.valueOf(EBOM.get(idx3).get("parent"));
			
			for (int i = 0; i < resLinkID.size(); i++) {
				String id_tmp = String.valueOf(resLinkID.get(i).get("identifier"));
				if (cur_tmp.equals(id_tmp)) {
					EBOM.get(idx3).put("cur_filename", String.valueOf(resLinkID.get(i).get("filename")));
				}
			}
			
			for (int k = 0; k < resLinkID.size(); k++) {
				String id_tmp2 = String.valueOf(resLinkID.get(k).get("identifier"));
				if (par_tmp.equals(id_tmp2)) {
					EBOM.get(idx3).put("par_filename", String.valueOf(resLinkID.get(k).get("filename")));
				}
			}
		}
		
//		System.out.println("-------List<Map<String, Object>> EBOMㅡ -------\n");
//		System.out.println(EBOM.size());
//		for (int ind = 0; ind < EBOM.size(); ind++) {
//			System.out.println(EBOM.get(ind).get("seq")+" : "+EBOM.get(ind).get("cur_filename")+" : "+EBOM.get(ind).get("par_filename") );
//		
//		}
//		
//		System.out.println("\n-------List<Map<String, Object>> resLinkID -------\n");
//		System.out.println(resLinkID.size());
//		for (int ind2 = 0; ind2 < resLinkID.size(); ind2++) {
//			System.out.println(resLinkID.get(ind2).get("identifier")+" : "+resLinkID.get(ind2).get("reflink")+" : "+resLinkID.get(ind2).get("filename"));
//		}
		
		ret_EBOM_LinkID.put("al_EBOM", EBOM);
		ret_EBOM_LinkID.put("al_Link", resLinkID);
		
		return ret_EBOM_LinkID;
	}
	
	private static void recurTreeChild(Node root_Node, List<Map<String, Object>> EBOM, List<Map<String, Object>> tmpLinkID) throws Exception{
		NodeList child_nList = root_Node.getChildNodes();
		int stack_seq = 1;
		for ( int i = 0; i < child_nList.getLength(); i++) {
			Map<String, Object> eMap_Tree = new HashMap<String, Object>();
			Map<String, Object> eMap_ID = new HashMap<String, Object>();
			Node child_Node = child_nList.item(i);
			
			if (child_Node.hasChildNodes()) {
				recurTreeChild(child_Node, EBOM, tmpLinkID);
			}
			
			if (child_Node.getNodeType() == root_Node.ELEMENT_NODE){
				Element eleR = (Element)root_Node;
				Element eleC = (Element)child_Node;				
				String curID = eleC.getAttribute("Identifier");
				String parentID = eleR.getAttribute("Identifier");
				if (curID == null || parentID == null || curID.length() == 0 || parentID.length() == 0) {
					
				} else {
					eMap_Tree.put("current", curID);
					eMap_Tree.put("parent", parentID);
					eMap_Tree.put("seq", stack_seq);
					eMap_ID.put("identifier", curID);
					stack_seq += 1;
				}
			}
			
			NodeList last_nList = child_Node.getChildNodes();
			for ( int k = 0; k < last_nList.getLength(); k++) {
				Node last_Node = last_nList.item(k);
				if (last_Node.getNodeType() == child_Node.ELEMENT_NODE) {
					Element ref_ele = (Element)last_Node;
					String reflinkID = ref_ele.getAttribute("Id");
					if (reflinkID == null || reflinkID.length() == 0) {
						
					} else {
						eMap_ID.put("reflink", reflinkID);
					}
				}
			}
			if (!eMap_Tree.isEmpty()) {
				EBOM.add(eMap_Tree);
			}
			if (!eMap_ID.isEmpty()) {
				tmpLinkID.add(eMap_ID);
			}
		}
	}
}

