plugins {
    alias(libs.plugins.donnations.jvm.library)
}

dependencies {
    // api: CampaignId is part of this module's public models and repository signatures.
    api(projects.core.model)
}
