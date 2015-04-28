


import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LDAPConnection {
    private String connectionName = "";

    private LdapContext ctx = null;
    static Control[] initcontrols;

    public LDAPConnection(String url, String username, String password,
            String authenSchema) {
        this(url, username, password, authenSchema, false);
    }

    public LDAPConnection(String url, String username, String password,
            String authenSchema, boolean isSSL, int ldapType) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial",
                "com.sun.jndi.ldap.LdapCtxFactory");

        env.put("java.naming.security.authentication", authenSchema);
        env.put("java.naming.provider.url", url);
        env.put("java.naming.security.principal", username);
        env.put("java.naming.security.credentials", password);
        env.put("com.sun.jndi.ldap.connect.pool", "true");

        env.put("com.sun.jndi.ldap.connect.pool.timeout", "300000");
        env.put("com.sun.jndi.ldap.connect.pool.prefsize", "50");
        env.put("com.sun.jndi.ldap.connect.pool.maxsize", "90");
        env.put("java.naming.referral", "follow");
        if (isSSL) {
            env.put("java.naming.security.protocol", "ssl");
            switch (ldapType) {
            case 1:
                String jrePath = System.getProperty("java.home");
                String keystore = "";
                if (jrePath.indexOf("\\") != -1)
                    keystore = jrePath + "\\lib\\security\\cacerts";
                else {
                    keystore = jrePath + "/lib/security/cacerts";
                }
                System.setProperty("javax.net.ssl.trustStore", keystore);
                break;
            default:
                env.put("java.naming.ldap.factory.socket",
                        "cn.com.chinautrust.idm.connector.ldap.ssl.AdvancedSocketFactory");
            }

        }

        try {
            this.ctx = new InitialLdapContext(env, null);
            env = null;
            if (initcontrols == null)
                initcontrols = this.ctx.getRequestControls();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LDAPConnection(String url, String username, String password,
            String authenSchema, boolean isSSL) {
        this(url, username, password, authenSchema, false, 2);
    }

    public void close() {
        if (this.ctx != null)
            try {
                this.ctx.close();
                this.ctx = null;
            } catch (NamingException e) {
                try {
                    this.ctx.close();
                } catch (NamingException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
    }

    public void reset() {
        try {
            this.ctx.setRequestControls(initcontrols);
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }

    public LdapContext getLdapContext() {
        return this.ctx;
    }

    public void setLdapContext(LdapContext ctx) {
        this.ctx = ctx;
    }

    public String getConnectionName() {
        return this.connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }
    
    public static void main(String[] args){

        LdapContext ctx = null;
        LDAPConnection conn = null;
        String adminName = "cn=Directory Manager";
        String adminPassword = "DirectoryManager";
        String ladpURL = "ldap://192.168.10.135:10000";
        String authoenSchema = "none";//none,simple,stong

        conn = new LDAPConnection(ladpURL, adminName, adminPassword,authoenSchema);

        try {
            ctx = conn.getLdapContext();
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String searchFilter = "(&(objectclass=simp-asset)(cn=Asset_1316159981577866))";
            String searchBase = "ou=Groups,dc=simp,dc=com";

            String rs[] = {
                    "cn",
                    "name",
                    "simp-asset-type",
                    "simp-asset-ip",
                    "simp-asset-connector-ip",
                    "simp-asset-connector-os-port",
                    "simp-asset-connector-os-manager",
                    "simp-asset-connector-os-manager-pwd" 
            };
            sc.setReturningAttributes(rs);
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            while (anser.hasMoreElements()) {

                SearchResult sr = anser.next();
                System.out.println(sr.getNameInNamespace());
                Attributes attrs = sr.getAttributes();
                String cn = attrs.get("cn").get(0).toString();
                String name = attrs.get("name").get(0).toString();
                String pwd = attrs.get("simp-asset-connector-os-manager-pwd").get(0).toString();
                System.out.println(cn + "\t" + name + "\t" + pwd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try{
                ctx.close();
            }catch(Exception ee){
                ee.printStackTrace();
            }
        }
    }
    
    
}