import groovy.text.SimpleTemplateEngine
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import java.text.SimpleDateFormat

class Penalite
{
    TimeDuration duration

    public Penalite(hms)
    {
        SimpleDateFormat hmsFormat = new SimpleDateFormat("hh:mm:ss")
        def date = hmsFormat.parse(hms)
        duration = new TimeDuration(0, date.getHours(), date.getMinutes(), date.getSeconds(), 0)
    }
}
// cotool classes
class Team {
    def dossard
    def puce
    def classement
    def nom
    def equipier1
    def equipier2
    def categorie

    def String toString()
    {
        "$nom ($categorie)"
    }
}

class Balise {
    def numero
    def epreuve
    def penalite

    def String toString()
    {
        "$epreuve:$numero"
    }
}

class Checkpoint {
    def time
    def ordrePassage

    def String toString()
    {
        def ordre = ordrePassage?" ($ordrePassage)":""
//        "${time.format("HH:mm:ss")} $ordre"
        "${time.format("HH:mm:ss")}"
    }
}

class Result {
    def puce
    def ordreArrivee
    def depart
    def arrivee
    def total
    def poincons
    Map checkpoints
    Map missed
    def penalite

    def String toString()
    {
        "($ordreArrivee) ${depart.format("HH:mm:ss")} ${arrivee.format("HH:mm:ss")} [$poincons] {${checkpoints} - $missed}"
    }
}

class Main {
    def colonne_premiere_balise
    def balises

    def inputFilePath
    def teamFilePath
    def resultFilePath

    def dernieres_balises = [:]
    def liste_balises = []
    def ordre_balises = [:]
    def epreuves

    def results
    def teams

// ---------------- result file template
    def template = '''
<%checkorder=true
def formatDuration(t)
{
    Date.parse("HH:mm:ss", "${t.hours}:${t.minutes}:${t.seconds}").format("HH:mm:ss")
}
%>Clt;Dossard;Nom Equipe;Equipier 1;Equiper 2;Sportident;Depart;Arrivee;Tps Final;Penalité;<%
balises.each
{
%>${it.epreuve};<%
}
%>balises manquées
;;;;;;;;;;<%
balises.each
{
%>${it.numero};<%
}
epreuves.each { epreuve, type_epreuve ->
%>${epreuve};<%
}
%>
<%
count=1
results.each { result ->
    team = teams[result.puce]
%>${count++};${team?.dossard};${team?.nom};${team?.equipier1};${team?.equipier2};${result.puce};${result.depart.format("HH:mm:ss")};${result.arrivee.format("HH:mm:ss")};${formatDuration(result.total)};${formatDuration(result.penalite)};<%
    balises.each { balise ->
        checkptnbr = "${balise.epreuve}-${balise.numero}"
        t = result.checkpoints[checkptnbr]
%>$t;<%
    }
result.missed.each { epreuve, balises ->
%>${balises.size};<%
}
%>
<%
}
'''
    // construction liste checkpoints (toutes epreuves confondues)
    def initBalises()
    {
        balises.each { epreuve, checkpoints_epreuve ->
            def derniere_balise
            def ordre_balises_epreuve = [:]
            def index_ordre=1
            checkpoints_epreuve.each { numero, penalite ->
                liste_balises.add(new Balise(numero:numero,epreuve:epreuve,penalite:penalite))
                derniere_balise = numero
                ordre_balises_epreuve["$numero"] = index_ordre++
            }
            dernieres_balises[epreuve]=derniere_balise
            ordre_balises[epreuve]=ordre_balises_epreuve
        }
    }

    def toTime(str)
    {
        if (str!=null)
        {
            Date.parse("HH:mm:ss", str)
        }
    }

// lecture fichier résultats
    def readResultFile()
    {
        def fileIn = new File(inputFilePath)
        def results = []
        def first=true;
        fileIn.splitEachLine(";") {fields ->
            if (first)
            {
                first = false
            }
            else
            {
                Result result = readResultLine(fields)
                results << result
            }
        }
        return results
    }

    Result readResultLine(fields) {
        def index_epreuve = 0
        def checkpoints = [:]
        def firstIndex = colonne_premiere_balise - 1
        def index_balise = 1
        def numeroPrecedent = 0
        def tmpStack = []
        (firstIndex..fields.size()).step(2) { index ->
            def numero = fields[index]
            def temps = fields[index + 1]
            def epreuve = (epreuves.keySet() as List)[index_epreuve]
            def checkpt_id = epreuve + "-" + numero
            def deja_pointe = (checkpoints[checkpt_id] != null)
            if (epreuves[epreuve] == "imposé") {
                if ((numero != null) && (numero < numeroPrecedent) && !deja_pointe) {
                    while (true) {
                        def pop = tmpStack.pop()
                        checkpoints[epreuve + "-" + pop] = null
                        if (tmpStack[-1] <= numero) break
                    }
                }
                if (!deja_pointe) {
                    checkpoints[checkpt_id] = new Checkpoint(ordrePassage: index_balise, time: toTime(temps))
                }
                tmpStack << numero
                index_balise++
                numeroPrecedent = numero
            } else {
                checkpoints[checkpt_id] = new Checkpoint(time: toTime(temps))
            }
            if ((index_epreuve < epreuves.size() - 1) && ("$numero" == "${dernieres_balises[epreuve]}")) {
                index_epreuve++
            }
        }
        new Result(puce: fields[1], ordreArrivee: fields[0], depart: toTime(fields[3]), arrivee: toTime(fields[4]), poincons: fields[5], checkpoints: checkpoints)
    }

// lecture fichier equipes
    def readTeamFile()
    {
        def fileIn = new File(teamFilePath)
        // read first 3 lines
        def teams = [:]
        def count = 0
        fileIn.splitEachLine(";") {fields ->
            if (count++>0)
            {
                def team = new Team(dossard:fields[2], puce:fields[8], nom:fields[3], equipier1:fields[4], equipier2:fields[5], categorie:fields[6])
                println team.dossard + " : " + team
                teams[fields[8]] = team
            }
        }
        return teams
    }

    def mapEpreuves()
    {
        def listeEpreuves = epreuves.keySet() as List
        def mapEpreuves = [:]
        listeEpreuves.each { epreuve ->
            mapEpreuves[epreuve]=[]
        }
        mapEpreuves
    }

    def calculPenalites()
    {
        results.each { result ->
            result.missed = mapEpreuves() // balises manquées
            def penalty = new TimeDuration(0, 0, 0, 0, 0) // initialisation penalités

            // verification passage checkpoints
            liste_balises.each { balise ->
                def balise_id = "${balise.epreuve}-${balise.numero}"
                if (result.checkpoints[balise_id]==null)
                {
                    result.checkpoints[balise_id]=""
                    result.missed[balise.epreuve] << balise.numero
                    penalty = penalty+balise.penalite.duration
                }
            }
            result.penalite =  penalty
            result.total = TimeCategory.minus(result.arrivee, result.depart) + penalty

            // vérification ordre balises
        }
    }

    def writeResultFile(ArrayList results, LinkedHashMap teams) {
        def binding = ["epreuves":epreuves, "balises": liste_balises, "results": results, "teams": teams]
        def engine = new SimpleTemplateEngine()
        def res = engine.createTemplate(template).make(binding)

        // fichier résultats final
        def outputfile = new File(resultFilePath)
        outputfile.withWriter { out ->
            out.println res.toString()
        }
    }

    def traitement()
    {
        println "--------------- Fichiers ------------------"
        println ("Fichier equipes : " + teamFilePath)
        println ("Fichier résultats : " + inputFilePath)
        println()

        // ---------------- Initialisations
        initBalises()

        println "--------------- Balises et pénalités ------------------"
        balises.each { epreuve, balises_epreuve ->
            print (" . " + epreuve + " (ordre " + epreuves[epreuve] + ") : ")
            def first = true
            balises_epreuve.each { numero, penalite ->
                if (!first)
                {
                    print " , "
                }
                else
                {
                    first = false
                }
                print (numero + " (penalité:"+penalite.duration+")")
            }
            println()
        }
        println()

        // ---------------- lecture des fichiers
        // fichier résultats
        println "--------------- Résultats ------------------"
        results = readResultFile()

        // fichier equipes
        teams = readTeamFile()

        // calcul penalités
        calculPenalites()

        // calcul classement (tri sur temps total)
        results.sort{it.total}.reverse()

        // ecriture fichier résultats
        writeResultFile(results, teams)
        println()

        println "--------------- Fichier résultat ------------------"
        println "=> " + resultFilePath
    }

}