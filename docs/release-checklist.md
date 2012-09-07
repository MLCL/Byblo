# Release Checklist for Project `mlcl-lib` #

This document is a step-by-step guide to producing a release of mlcl-lib. It's
a little bit complicated, because both Maven and Git need to be coordinated, 
but should be manageable.

It is assumed that all development work is committed to the `develop` branch of the SCM, while `master` is reserved for releases.


### Finalise the `develop` branch ###

 1. Check that all the unit tests pass.
	```
		$ mvn test
	```

 2. Attempt to resolve any build warnings that occur.

 3. Check that there are no SNAPSHOT dependencies in the POM.

 4. Check the documentation is up to date. In particular read through `README.md`.

 5. Check the licence headers are all correct and up date
	```
		$ mvn license:check
	```
	and if necessary resolve any issues
	```
		$ mvn license:format
	```
 6. Check that there are no uncommitted changes in the sources



### Produce the final tagged revision ###

 1. Checkout `master` branch from SCM.
	```
		$ git checkout master
	```

 2. Merge the `develop` branch into `master`.
	```
		$ git merge --no-ff develop
	```

 3. Change the version in the POM from x-SNAPSHOT to the new version. E.g:
	```
		...
		<version>0.2</version>
		...
	```
 4. Transform the SCM information in the POM to include the final destination of the tag. E.g:
	```
 		<scm>
	        <tag>mlcl-lib-0.2</tag>
			...
    	</scm>
	```

 5. Run the unit tests (again) to confirm everything is in working order.

 6. Commit the modified POMs

 7. Tag the code in the SCM with a versioned name
	```
		$ git tag mlcl-lib-0.2
	```
	
 8. Deploy the tagged revision to the Maven repository:
	```
		$ mvn -P release deploy
	```
	(You will be required to sign the artefacts, and consequently will be prompted for your GPG passphrase.)

### Produce the API docs site ###

The API javadocs are stored in a GH-Pages site on GitHub, which uses an orphaned branch of the main repository (named `gh-pages`). To update the site
we must create a new clone of the repository, because we must simultaneously produce the site (requiring the `master` branch), and upload the site (requiring the `gh-pages` branch.)

 1. Produce the site
	```
		$ mvn site
	```

 2. Create a new clone of the repository on the `gh-pages` branch.
	```
		$ git clone https://github.com/MLCL/MLCLLib.git target/siterepo
		$ cd target/siterepo
		$ git checkout gh-pages
	```
	
 3. Update the site by deleting everything then copying the new site
	```
		$ git rm -r *
		$ cp -R ../site/* .
		$ git add .
		$ git commit -m "Update API docs"
		$ git push origin gh-pages
	```

### Sort out the `develop` branch for continued work ###

 1. Merge the `master` branch back into `develop`.
	```
		$ git checkout develop
		$ git merge --no-ff master
	```

 2. Bump the version in the POM to a new value y-SNAPSHOT
	```
		...
		<version>0.3-SNAPSHOT</version>
		...
 		<scm>
	        <tag>HEAD</tag>
			...
    	</scm>
	```

 3. Commit the modified POM


### All done! ###

You may now bask in the warm glow of this modest accomplishment. Good job... Now get back to work!