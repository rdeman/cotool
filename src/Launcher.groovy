public class Launcher {

    def static script_sample = '''
// ---------------- parametres
// dossier contenant les fichiers
path = 'C:\\\\cotool\\\\'

// fichier résultats
inputFileName = 'resultats.csv'
colonne_premiere_balise=7

// fichier des equipes
teamFileName = 'inscrits.csv'

// balises et penalités
epreuves=[CO:'imposé',VTT:'libre']

penalite1 = new Penalite("0:30:00")
penalite2 = new Penalite("00:40:00")

balises=[:]
balises['CO']=[1:penalite1,2:penalite1,3:penalite1,4:penalite1,5:penalite2]
balises['VTT']=[6:penalite2,7:penalite1,8:penalite1,9:penalite1,3:penalite2,10:penalite2]

// traitement
new Main(
        colonne_premiere_balise:colonne_premiere_balise,
        balises:balises,
        epreuves:epreuves,
        inputFilePath:path+inputFileName,
        teamFilePath:path+teamFileName,
        resultFilePath:path+inputFileName.replace('.','_out.')
).traitement()
'''
	public static void main(String[] args) {
        if (args.length == 0)
        {
            usage()
        }
        else
        {
            GroovyShell.main(args);
        }
        System.exit(0)
	}

    public static void usage()
    {
        println ("Utilisation : ")
        println ("java -jar cotool.jar script.groovy")
        println (" où script.groovy est un script de la forme : ")
        println (script_sample)
    }
}
