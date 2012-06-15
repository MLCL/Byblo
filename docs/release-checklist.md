# Release Checklist #

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

 3. Check that there are no uncommitted changes in the sources

 4. Check that there are no SNAPSHOT dependencies in the POM.

 5. Check the licence headers are all correct and up date
	```
		$ mvn -P license license:check
	```
	and if necessary resolve any issues
	```
		$ mvn -P license license:format
	```


### Produce the final tagged revision ###

 5. Checkout `master` branch from SCM.
	```
		$ git checkout master
	```

 6. Merge the `develop` branch into `master`.
	```
		$ git merge --no-ff develop
	```

 7. Change the version in the POM from x-SNAPSHOT to the new version. E.g:
	```
		...
		<version>0.2</version>
		...
	```
 8. Transform the SCM information in the POM to include the final destination of the tag. E.g:
	```
 		<scm>
	        <tag>mlcl-lib-0.2</tag>
			...
    	</scm>
	```

 9. Run the unit tests (again) to confirm everything is in working order.

 10. Commit the modified POMs

 11. Tag the code in the SCM with a versioned name
	```
		$ git tag mlcl-lib-0.2
	```
	
 12. Deploy the tagged revision to the Maven repository:
	```
		$ mvn -P release deploy
	```
	(You will be required to sign the artefacts, and consequently will be prompted for your GPG passphrase.)


### Sort out the `develop` branch for continue work ###

 13. Merge the `master` branch back into `develop`.
	```
		$ git checkout develop
		$ git merge --no-ff develop
	```

 14. Bump the version in the POM to a new value y-SNAPSHOT
	```
		...
		<version>0.3-SNAPSHOT</version>
		...
 		<scm>
	        <tag>HEAD</tag>
			...
    	</scm>
	```

 15. Commit the modified POM


### All done! ###

You may now bask in the warm glow of this modest accomplishment. Good job... Now get back to work!