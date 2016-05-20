package com.lvt4j.android;

import java.util.Map;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * WebService工具类，需要ksoap2-android.jar包的支持
 * @author LV
 *
 */
public class TWebService {
	private String url;
	private String nameSpace;
	private int soapVer = SoapEnvelope.VER10;
	private boolean isDotNet = false;

	//[start] 初始化设置
	public void setUrl(String url) {
		this.url = url;
	}
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}
	public void setDotNet(boolean isDotNet) {
		this.isDotNet = isDotNet;
	}
	public void setSoap10() {
		soapVer = SoapEnvelope.VER10;
	}
	public void setSoap11() {
		soapVer = SoapEnvelope.VER11;
	}
	public void setSoap12() {
		soapVer = SoapEnvelope.VER12;
	}
	//[end]
	public Object doWebService(String methodName,Map<String, Object>args) throws Exception {  
        // 指定WebService的命名空间和调用的方法名  
        SoapObject rpc = new SoapObject(nameSpace, methodName);  
  
        if (args!=null) {
			for (String key : args.keySet()) {
				rpc.addProperty(key, args.get(key));
			}
		}
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本  
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(soapVer);  
  
        envelope.bodyOut = rpc;  
        // 设置是否调用的是dotNet开发的WebService  
        envelope.dotNet = isDotNet;  
        // 等价于envelope.bodyOut = rpc;  
        envelope.setOutputSoapObject(rpc);  
  
        HttpTransportSE transport = new HttpTransportSE(url);  
        // 调用WebService  
        transport.call(null, envelope);  
        // 获取返回的数据  
        SoapObject object = (SoapObject) envelope.bodyIn;  
        
        return object.getProperty(0);
    }  
}
