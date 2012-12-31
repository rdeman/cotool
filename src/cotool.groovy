import groovy.time.*

// ---------------- parametres
// dossier contenant les fichiers
path = 'D:\\dev\\groovy\\cotool\\'
// fichier résultats
inputFileName = 'result_test.csv'
colonne_premiere_balise=7
// fichier des equipes
teamFileName = 'Inscrits2012.csv'

// balises et penalités
penalite1 = new Penalite("0:30:00")
penalite2 = new Penalite("00:40:00")
epreuves=[CO:'imposé',VTT:'libre']
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
