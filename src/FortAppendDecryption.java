import java.lang.reflect.*;

import com.crypto.PWDTEXT;


public class FortAppendDecryption {

	private static PWDTEXT aa = new PWDTEXT();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//com.crypto.PWDTEXT.main(new String[]{"B5348BEE4161DD81"});
		//System.out.println(decrypPwdForAssetAccount("E33AAAAC193AACF08A2AEA786B562145"));
		//System.out.println(decrypPwdForAssetAccount("BB861761BC433224"));
		
	}
	
	public static String decrypPwdForAssetAccount(String securityText){
		
		Method[] ms = aa.getClass().getDeclaredMethods();
		for(Method m : ms){
			//System.out.println(m.getName());
			if("a".equals(m.getName())){}else{continue;};
			try{
				m.setAccessible(true);
				Class[] cs = m.getParameterTypes();
				if(cs.length == 2 && cs[0].getName().equals("java.lang.String")){
					Object o = m.invoke(aa, a.a("xzy(/+*,'/* e"),securityText);
					//System.out.println(o);
					return o.toString();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}		
		return null;
	}

}