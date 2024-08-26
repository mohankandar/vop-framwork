mavenLibraryPipeline {

    //Specify to use the fortify maven plugin, instead of the Ant task to execute the fortify scan
    useFortifyMavenPlugin = true

    //Skip Section
    //skipTests = true
    //skipSonar = true
    skipFortify = true

    buildsToKeep = "5"
    
    /*************************************************************************
    * Tech Docs Build Configuration
    *************************************************************************/

    // enabled publishing tech docs to s3
    publishTechDocsToS3 = true

    s3TechDocsBucketName = "default/api/vop-framework"
    s3TechDocsEntity = "vop-dev-bih-tech-docs-bucket"
}
