//--------------------------------------------------------------------------
def srcDir = 'src'
def binDir = 'bin'
def buildDir = 'build'

//--------------------------------------------------------------------------
def ant = new AntBuilder()

def bin = new File(binDir)
println bin.getAbsolutePath()
if (bin.exists())
{
    bin.mkdir()
}
def build = new File(buildDir)
if (build.exists())
{
    build.mkdir()
}

def srcFiles = ['cotool_classes.groovy','Launcher.groovy']
ant.echo( "Compiling ${srcFiles}" )
srcFiles.each { file ->
    org.codehaus.groovy.tools.FileSystemCompiler.main( [ "${srcDir}/${file}", "-d${binDir}" ] as String[] )
}

def GROOVY_HOME = new File( System.getenv('GROOVY_HOME') )
if (!GROOVY_HOME.canRead()) {
  ant.echo( "Missing environment variable GROOVY_HOME: '${GROOVY_HOME}'" )
  return
}

ant.jar( destfile: "${buildDir}/cotool.jar", compress: true, index: true ) {
    fileset( dir: "./${binDir}", includes: '*.class' )
}

ant.jar( destfile: "${buildDir}/cotool_full.jar", compress: true, index: true ) {
  fileset( dir: "./${binDir}", includes: '*.class' )
  zipgroupfileset( dir: GROOVY_HOME, includes: 'embeddable/groovy-all-*.jar', excludes: 'embeddable/groovy-all-*-indy.jar' )
  zipgroupfileset( dir: GROOVY_HOME, includes: 'lib/commons*.jar' )
  manifest {
    attribute( name: 'Main-Class', value: 'Launcher' )
  }
}

ant.echo ("Jar files have been built in ${(new File(buildDir)).getAbsolutePath()}")