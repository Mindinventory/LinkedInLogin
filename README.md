# Linkedin Login 

This provides the webview to Login with Linked in and getting callback to your activity or fragment

### Key Features

* Integrate Linkedin Login

# Usage

* Dependencies

    Step 1. Add the JitPack repository to your build file
    
    Add it in your root build.gradle at the end of repositories:

    ```groovy
	    allprojects {
		    repositories {
			    ...
			    maven { url 'https://jitpack.io' }
		    }
	    }
    ``` 

    Step 2. Add the dependency
    
    Add it in your app module build.gradle:
    
    ```groovy
        dependencies {
            ...
            implementation 'com.github.Mindinventory:LinkedInLogin:0.0.1'
        }
    ``` 
    
    
* Implementation

    Step 1.Call MiLinkedinActivity from your activity class
    
    ```kotlin
        MiLinkedInActivity.startLinkedInActivityForDetails(
            this,
            "CLIENT_ID",
            "CLIENT_SECRET",
            "REDIRECT_URI",
            "STATE_VALUE"
        )
    ```
    Step 2.Get data from onActivityResult Method
    
    ```kotlin
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MiLinkedInActivity.REQUEST_CODE -> {
                val intent = data?.extras
                if (intent != null) {
                    when {
                        intent.containsKey(MiLinkedInActivity.KEY_LINKEDIN_DETAIL_DATA) -> {
                            val linkedInUser =
                                data.getParcelableExtra<LinkedInUserDetails>(MiLinkedInActivity.KEY_LINKEDIN_DETAIL_DATA)
                            if (linkedInUser != null) {
                                // do what ever you want with details
                            }
                        }
                    }
                }
            }
        }
    }
    
   ```
### Requirments   

* Android X
* Min sdk >=16
    


