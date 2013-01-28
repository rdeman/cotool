// ---------------- parametres
// dossier contenant les fichiers
path = 'D:\\dev\\groovy\\cotool\\'

// fichier résultats
inputFileName = 'result_test_2.csv'
colonne_premiere_balise=7

// fichier des equipes
teamFileName = 'Inscrits2012.csv'

// balises et penalités
epreuves=[CO1:'imposé',VTT:'libre',CO2:'libre']

penalite1 = new Penalite("0:30:00")
penalite2 = new Penalite("00:40:00")

balises=[:]
balises['CO1']=[1:penalite1,2:penalite1,3:penalite1,4:penalite1,5:penalite2]
balises['VTT']=[6:penalite2,7:penalite1,8:penalite1,9:penalite1,3:penalite2,10:penalite2]
balises['CO2']=[11:penalite2,12:penalite1,13:penalite1,14:penalite2]

// traitement
new Main(
        colonne_premiere_balise:colonne_premiere_balise,
        balises:balises,
        epreuves:epreuves,
        inputFilePath:path+inputFileName,
        teamFilePath:path+teamFileName,
        resultFilePath:path+inputFileName.replace('.','_out.')
).traitement()
