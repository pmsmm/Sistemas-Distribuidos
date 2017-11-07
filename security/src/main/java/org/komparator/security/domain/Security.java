package org.komparator.security.domain;

//Securoty Manager
//applications when registering to uddi save here infomration like theirs wsName
//so that in handlers we can identofy who is sendind the maessage and who is receiving
public class Security {

	private Security() {
	}

	
	private String wsName = null; //name of the apllication
	private String keyAlias = null; //keyAlias name
    private String keyPass = null;//privateKey pass
    private String keyStorePass = null; //pass of the keyStore
    private String caCert = null; //filepath to the ca certificate
    private String mineCert = null; //name of uddi publishing
    private String certPath = null; //filepath to the application public certificate
    private String keyStorePath = null;//filepath to the keyStore
    private String nameMediator = null;
	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final Security INSTANCE = new Security();
	}

	public static synchronized Security getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public String getName(){
		return wsName;
	}
	public void setName(String name){
		this.wsName = name;
	}
	public void setCaCert(String cCert){
		this.caCert= cCert;
	}
	public String getCACert(){
		return caCert;
	}
	public void setMineCertPath(String mCert){
		this.certPath = mCert;
	}
	public String getMineCertPath(){
		return certPath;
	}
	public void setKeyStorePath(String ksPath){
		this.keyStorePath = ksPath;
	}
	public String getKeyStorePath(){
		return keyStorePath;
	}
	public void setKSPass(String ksPass){
		this.keyStorePass= ksPass;
	}
	public String getKSPass(){
		return keyStorePass;
	}
	public void setKeyAlias(String kAlias){
		this.keyAlias = kAlias;
	}
	public String getKeyAlias(){
		return keyAlias;
	}
	public void setKeyPass(String kPass){
		this.keyPass = kPass;
	}
	public String getKeyPass(){
		return keyPass;
	}
	public void setMineCert(String cert){
		this.mineCert = cert;
	}
	public String getMineCert(){
		return mineCert;
	}
	public void setMediator(String name){
		this.nameMediator = name;
	}
	public String getMediator(){
		return nameMediator;
	}
}	