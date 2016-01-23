import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

public class GenerateurStub {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// On récupère le nom de la classe 
		String nomClasse = args[0] ;
		creerClasses(nomClasse); 

	}
	
	public static void creerClasses(String nomClasse) {
		// TODO Auto-generated method stub
		// On récupère le nom de la classe 
		String nomItf = nomClasse +"_itf" ;
		String nomStub = nomClasse + "_stub" ;
		Class classe = null ;
		System.out.println(nomStub) ; 
		try {
			classe = Class.forName(nomClasse);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		FileWriter stubFL = null ; 
		FileWriter itfFL = null ; 
		//Création de la classe nomClasse_stub.java 
		try {
			stubFL = new FileWriter(nomStub + ".java");
			itfFL = new FileWriter(nomItf + ".java") ; 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//pour écrire dans la classe object_stub
		PrintWriter stubPW = new PrintWriter(stubFL) ;
		PrintWriter itfPW = new PrintWriter(itfFL) ; 
		
		//écriture de la première ligne dans la classe
		stubPW.println("public class " + nomStub + " extends SharedObject implements " + nomItf + ", java.io.Serializable {\n"); 
		itfPW.println("public interface " + nomItf + " extends SharedObject_itf {\n");
		
		//attribut dans la classe stub
		stubPW.println("\tprivate " + nomClasse + " object;\n" );
		
		//récupération des méthodes de la classe
		Method[] methodes = classe.getMethods() ;
		//parcours de la liste des méthodes + récupération des différentes propriétés
		
		for (int i=0;i<methodes.length;i++) {
			Method methode = methodes[i] ;
			String nomMethode = methode.getName() ;
			if (!nomMethode.equals("wait") && !nomMethode.equals("equals") && !nomMethode.equals("toString") && !nomMethode.equals("hashCode") && !nomMethode.equals("getClass") && !nomMethode.equals("notify") && !nomMethode.equals("notifyAll"))
			{
			System.out.println(nomMethode);
			int modifiers = methode.getModifiers() ; 
			Class<?> returnType = methode.getReturnType() ;
			Class<?>[] parametersTypes = methode.getParameterTypes();
			int nbParametres = parametersTypes.length ; 
			System.out.println(nbParametres);
			
			//écriture de la première ligne de la méthode (sans retour à la ligne -> print)
			stubPW.print("\tpublic " + returnType.getName() + " " + nomMethode + " (");
			itfPW.print("\tpublic " + returnType.getName() + " " + nomMethode + " (");
			
			//écriture des paramètres
			
			for(int j=0;j<nbParametres;j++) {
				String nomParametre = parametersTypes[j].getName() ;
				System.out.println(nomParametre);
				stubPW.print(nomParametre + " p" + j);
				itfPW.print(nomParametre + " p" + j);
				
				//s'il reste des paramètres, on écrit une virgule
				if (j!=nbParametres-1) {
					stubPW.print(",");
					itfPW.print(",");
				}
				
			}
			
			//fin de la déclaration pour l'interface
			itfPW.println(");"); 
			
			//fin de la déclaration pour le stub
			stubPW.println(") { ");
			stubPW.println("\t\t" + nomClasse + " object = " + "(" + nomClasse + ")" + " obj;");
			if (returnType.getName().equals("void")) {
				stubPW.print("\t\tobject." + nomMethode + "(");
				for (int k=0;k<nbParametres;k++) {
					stubPW.print("p" + k);
					if (k!=nbParametres-1) {
						stubPW.print(",");

					}
					
				}
				stubPW.print(");");

			}
			else {
				stubPW.print("\t\treturn object." + nomMethode + "(");
				for (int k=0;k<nbParametres;k++) {
					stubPW.print("p" + k);
					if (k!=nbParametres-1) {
						stubPW.print(",");

					}
					
				}
				stubPW.print(");");

			}
			stubPW.println("\n\t}");
			
			}

			
		}
	stubPW.print("\n}");
	itfPW.print("\n}");
	stubPW.close();
	itfPW.close();

	}

}
