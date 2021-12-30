package com.yann.springboot.war.test.vo;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;

public class Test {
	public static void main(String[] args) throws Exception {
		String jsonStr = Test.getJsonString();
		SkElement skElement = JSON.parseObject(jsonStr, SkElement.class);
		if(skElement!=null && skElement.getData()!=null && skElement.getData().getTrace()!=null && skElement.getData().getTrace().getSpans()!=null) {
			int size = skElement.getData().getTrace().getSpans().size();
			
			List<SpanElement> orgSpans = skElement.getData().getTrace().getSpans();
			Test.attempt2(orgSpans, size);
		}
	}
	
	public static void attempt2(List<SpanElement> orgSpans,int size) {
		TreeNode<SpanElement> root = new TreeNode<>();
		root.setValue(orgSpans.get(0));
		TreeNode<SpanElement> newNode = null;
		for(int i=1;i<size;i++) {
			newNode = new TreeNode<>();
			newNode.setValue(orgSpans.get(i));
			drawTree(root,newNode);
		}
		
		Test.printTree(root, 0);
	}
	public static void printTree(TreeNode<SpanElement> node,int idx) {
		for(int i=0;i<2*idx;i++) {
			System.out.print(" ");
		}
		System.out.print(node.getValue().getSegmentId().substring(node.getValue().getSegmentId().lastIndexOf(".")+1)+"   "+node.getValue().getSpanId()+"   "+node.getValue().getParentSpanId()+"   "+node.getValue().getEndpointName()+"   "+node.getValue().getServiceCode());
		if(CollectionUtils.isEmpty(node.getValue().getRefs())) {
			System.out.println("");
		}else {
			System.out.println("   "+node.getValue().getRefs().get(0).getParentSegmentId().substring(node.getValue().getRefs().get(0).getParentSegmentId().lastIndexOf("."))+"   "+node.getValue().getRefs().get(0).getParentSpanId());
		}
		if(!CollectionUtils.isEmpty(node.getChilds())) {
			idx++;
			for(TreeNode<SpanElement> cNode : node.getChilds()) {
				printTree(cNode,idx);
			}
		}
	}
	
	public static boolean drawTree(TreeNode<SpanElement> node,TreeNode<SpanElement> newNode) {
		if(!CollectionUtils.isEmpty(newNode.getValue().getRefs())
				&& newNode.getValue().getRefs().get(0).getParentSegmentId().equals(node.getValue().getSegmentId()) 
				&& newNode.getValue().getRefs().get(0).getParentSpanId() == node.getValue().getSpanId()) {
			if(node.getChilds()==null) {
				node.setChilds(new ArrayList<>());
			}
			node.getChilds().add(newNode);
			return true;
		}
		if(newNode.getValue().getSegmentId().equals(node.getValue().getSegmentId())
				&& newNode.getValue().getParentSpanId()==node.getValue().getSpanId()) {
			if(node.getChilds()==null) {
				node.setChilds(new ArrayList<>());
			}
			node.getChilds().add(newNode);
			return true;
		}
		
		if(!CollectionUtils.isEmpty(node.getChilds())){
			for(int i=0;i<node.getChilds().size();i++) {
				if(drawTree(node.getChilds().get(i),newNode)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void attempt1(List<SpanElement> orgSpans,int size) {
		List<SpanElement> tmpSpans = new ArrayList<>();
		boolean hasLoop = false;
		for(int i=0;i<size;i++) {
			System.out.println("===========> "+i);
			if(i==0) {
				System.out.println("a-add "+i);
				tmpSpans.add(orgSpans.get(i));
			}else {
				// 当前遍历的元素和 缓存起来的最后一个元素比较 segmentid 不同
				// 并且当前遍历的元素 refs 不为空
				// 并且当前遍历的元素的 refs 的 parenentSegmentId 和 缓存起来的最后一个元素比较 segmentid 相同
				if(!orgSpans.get(i).getSegmentId().equals(tmpSpans.get(tmpSpans.size()-1).getSegmentId()) &&
						!CollectionUtils.isEmpty(orgSpans.get(i).getRefs()) && 
						orgSpans.get(i).getRefs().get(0).getParentSegmentId().equals(tmpSpans.get(tmpSpans.size()-1).getSegmentId())) {
					// 如果不是最后一个元素
					if(i<size-1) { 
						System.out.println("b-add "+i);
						tmpSpans.add(orgSpans.get(i));
					}else {
						System.out.println("c-add "+i);
						tmpSpans.add(orgSpans.get(i));
						// 如果是最后一个元素（叶子），判断loop
						if(checkLoop(i,tmpSpans)) {
							hasLoop = true;
						}else {
							System.out.println("a-remove ");
							tmpSpans.remove(tmpSpans.size()-1);
						}
					}
				}else if(orgSpans.get(i).getSegmentId().equals(tmpSpans.get(tmpSpans.size()-1).getSegmentId())) {
				// 	当前遍历的元素和 缓存起来的最后一个元素比较 segmentid 相同
					// 不是最后一个元素
					if(i<size-1) {
						// 如果后面一个元素的 refs 不为空
						// 并且后面一个元素的 refs 的 parenentSegmentId 和 当前元素比较 segmentid 相同
						if(!CollectionUtils.isEmpty(orgSpans.get(i+1).getRefs()) ) { 
							if(orgSpans.get(i+1).getRefs().get(0).getParentSegmentId().equals(orgSpans.get(i).getSegmentId())) { 
								System.out.println("d-add "+i);
								tmpSpans.add(orgSpans.get(i));
								continue;
							}
						}else {
							System.out.println("e-add "+i);
							tmpSpans.add(orgSpans.get(i));
							//如果为空，说明是叶子了
							if(checkLoop(i,tmpSpans)) {
								hasLoop = true;
							}else {
								System.out.println("b-remove");
								tmpSpans.remove(tmpSpans.size()-1);
							}
						}
						//是不是这个分支的最后一个元素
						if(!orgSpans.get(i).getSegmentId().equals(orgSpans.get(i+1).getSegmentId())) {
							System.out.println("c-remove");
							tmpSpans.remove(tmpSpans.size()-1);
							Iterator iter = tmpSpans.iterator();
						}
					}else { // 如果是最后一个元素（叶子），判断loop
						if(checkLoop(i,tmpSpans)) {
							hasLoop = true;
						}else {
							System.out.println("d-remove");
							tmpSpans.remove(tmpSpans.size()-1);
						}
					}
				}
			}
		}
		if(hasLoop) {
			System.out.println("trace: "+orgSpans.get(0).getTraceId()+", There is a loop.");
		}else {
			System.out.println("trace: "+orgSpans.get(0).getTraceId()+", There is no loop.");
		}
	}
	
	public static boolean checkLoop(int idx,List<SpanElement> tmpSpans) {
		boolean hasLoop = false;
		Stack<String> stack = new Stack<>();
		for(int i=0;i<tmpSpans.size();i++) {
			if(i==0) {
				stack.push(tmpSpans.get(i).getServiceCode());
			}
			if(stack.peek().equalsIgnoreCase(tmpSpans.get(i).getServiceCode())) {
				continue;
			}else {
				stack.push(tmpSpans.get(i).getServiceCode());
			}
			
		}
		List<String> strs = new ArrayList<>();
		while(!stack.empty()) {
			strs.add(stack.pop());
		}
		for(int i=strs.size()-1;i>=0;i--) {
			System.out.print(strs.get(i)+"--->");
		}
		System.out.println("");
		System.out.println("temporary check at:"+tmpSpans.get(tmpSpans.size()-1).getSegmentId()+
				", endpoint:"+tmpSpans.get(tmpSpans.size()-1).getEndpointName()+
				", spanid:"+tmpSpans.get(tmpSpans.size()-1).getSpanId()+
				", parentSpanid:"+tmpSpans.get(tmpSpans.size()-1).getParentSpanId()+
				", serviceCode:"+tmpSpans.get(tmpSpans.size()-1).getServiceCode()+", result: "+hasLoop);
		return hasLoop;
	}
	
	public static String getJsonString() throws Exception {
		File jsonFile = new File("src/main/resources/sk.json");
		FileInputStream fi = new FileInputStream(jsonFile);
		FileChannel fic = fi.getChannel();
		ByteBuffer dsts = ByteBuffer.allocateDirect(512);
		StringBuilder sb = new StringBuilder();
		while(true) {
			dsts.clear();
			int read = fic.read(dsts);
			if(-1==read) {
				break;
			}
			dsts.flip();//翻转
			while(dsts.remaining()>0) {
				byte b = dsts.get();//从bytebuffer中读取
				sb.append((char)b);
			}
		}
		fic.close();
		fi.close();
		return sb.toString();
	}
}

class TreeNode<SpanElement> {
	SpanElement value;

    List<TreeNode<SpanElement>> childs;

	public SpanElement getValue() {
		return value;
	}
	public void setValue(SpanElement value) {
		this.value = value;
	}
	public List<TreeNode<SpanElement>> getChilds() {
		return childs;
	}
	public void setChilds(List<TreeNode<SpanElement>> childs) {
		this.childs = childs;
	}
}

class RefElement{
	private String traceId;
	private String parentSegmentId;
	private int parentSpanId;
	public String getTraceId() {
		return traceId;
	}
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	public String getParentSegmentId() {
		return parentSegmentId;
	}
	public void setParentSegmentId(String parentSegmentId) {
		this.parentSegmentId = parentSegmentId;
	}
	public int getParentSpanId() {
		return parentSpanId;
	}
	public void setParentSpanId(int parentSpanId) {
		this.parentSpanId = parentSpanId;
	}
	@Override
	public String toString() {
		return "RefElement [traceId=" + traceId + ", parentSegmentId=" + parentSegmentId + ", parentSpanId="
				+ parentSpanId + "]";
	}
}

class SpanElement{
	private String traceId;
	private String segmentId;
	private int spanId;
	private int parentSpanId;
	private String serviceCode;
	private String endpointName;
	private List<RefElement> refs;
	public String getTraceId() {
		return traceId;
	}
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	public String getSegmentId() {
		return segmentId;
	}
	public void setSegmentId(String segmentId) {
		this.segmentId = segmentId;
	}
	public int getSpanId() {
		return spanId;
	}
	public void setSpanId(int spanId) {
		this.spanId = spanId;
	}
	public int getParentSpanId() {
		return parentSpanId;
	}
	public void setParentSpanId(int parentSpanId) {
		this.parentSpanId = parentSpanId;
	}
	public String getServiceCode() {
		return serviceCode;
	}
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	public String getEndpointName() {
		return endpointName;
	}
	public void setEndpointName(String endpointName) {
		this.endpointName = endpointName;
	}
	public List<RefElement> getRefs() {
		return refs;
	}
	public void setRefs(List<RefElement> refs) {
		this.refs = refs;
	}
	@Override
	public String toString() {
		return "SpanElement [traceId=" + traceId + ", segmentId=" + segmentId + ", spanId=" + spanId + ", parentSpanId="
				+ parentSpanId + ", serviceCode=" + serviceCode + ", endpointName=" + endpointName + ", refs=" + refs
				+ "]";
	}
}
class TraceElement{
	private List<SpanElement> spans;
	public List<SpanElement> getSpans() {
		return spans;
	}
	public void setSpans(List<SpanElement> spans) {
		this.spans = spans;
	}
}
class DataElement{
	private TraceElement trace;
	public TraceElement getTrace() {
		return trace;
	}
	public void setTrace(TraceElement trace) {
		this.trace = trace;
	}
}
class SkElement{
	private DataElement data;
	public DataElement getData() {
		return data;
	}
	public void setData(DataElement data) {
		this.data = data;
	}
}