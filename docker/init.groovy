import jenkins.model.*
import hudson.security.*
import org.apache.commons.lang.RandomStringUtils
import jenkins.install.InstallState

def instance = Jenkins.getInstance()
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
def strategy = new GlobalMatrixAuthorizationStrategy()

// Create admin user
def adminUsername = "devcloudninjas"
def adminPassword = RandomStringUtils.randomAlphanumeric(16)
hudsonRealm.createAccount(adminUsername, adminPassword)
strategy.add(Jenkins.ADMINISTER, adminUsername)

println "Created admin user: ${adminUsername} with password: ${adminPassword}"

// Array of unique names
def names = ["mrakinwale", "mrayo", "mrdorby", "mrgerald", "mrgodwin", "mribeh", "mrinyene", "mriremede", "mrmackle", "mrolumide", "mrosagie", "msadekemi", "msaudrey", "msfavor", "msndeye"]

// Create users with unique names
names.each { name ->
    def username = name.toLowerCase()
    def password = RandomStringUtils.randomAlphanumeric(12)
    hudsonRealm.createAccount(username, password)
    strategy.add(Jenkins.READ, username)
    println "Created user: ${username} with password: ${password}"
}

instance.setSecurityRealm(hudsonRealm)
instance.setAuthorizationStrategy(strategy)

// Install plugins
def pluginManager = instance.getPluginManager()
def updateCenter = instance.getUpdateCenter()
updateCenter.updateAllSites()

def plugins = [
    "git",
    "workflow-aggregator",
    "docker-workflow",
    "blueocean",
    "credentials-binding",
    "timestamper",
    "publish-over-ssh",
    "docker-build-publish",
    "terraform",
    "ansible"
]

plugins.each { pluginName ->
    if (!pluginManager.getPlugin(pluginName)) {
        println "Installing ${pluginName} plugin"
        def plugin = updateCenter.getPlugin(pluginName)
        if (plugin) {
            plugin.deploy()
        } else {
            println "WARNING: ${pluginName} plugin not found in Update Center"
        }
    } else {
        println "${pluginName} plugin already installed"
    }
}

// Set Jenkins to RUNNING state
instance.setInstallState(InstallState.RUNNING)

instance.save()

// Wait for plugin installation to complete
while (pluginManager.isDownloadInProgress()) {
    println "Waiting for plugin installation to complete"
    sleep(3000)
}

println "Plugin installation completed"

// Restart Jenkins to apply changes
instance.restart()
