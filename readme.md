# flink-gcs-fs
Apache Flink file system for GCS - Google Cloud Storage

## Application Credentials
The GCS sink needs to authenticate to the GCP - Google Cloud Platform - API. To authenticate an application like the GCS sink, GCP provides the [Service Account](https://cloud.google.com/iam/docs/understanding-service-accounts). A service account is a special type of Google account that belongs to your application or a virtual machine (VM), instead of to an individual end user. Your application assumes the identity of the service account to call Google APIs, so that the users aren't directly involved.  

The GCS sink needs a service account that has access to GCS. 

To create a service account type:

```bash
# create a service account
$ gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME
# create a binding (member+role) and add it to the project  
$ gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member "serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com" \
    --role "roles/owner"
# create a service account JSON key file
$ gcloud iam service-accounts keys create $CREDS_FILE_NAME \
    --iam-account $SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com
```

Once you have the JSON key file, you must add the following property to
`core-site.xml` on your server.

```xml
<property>
  <name>google.cloud.auth.service.account.json.keyfile</name>
  <value>/path/to/keyfile</value>
  <description>
    The JSON key file of the service account used for GCS
    access when google.cloud.auth.service.account.enable is true.
  </description>
</property>
```

```bash
# set the environment var and launch the application
export GOOGLE_APPLICATION_CREDENTIALS="./$CREDS_FILE_NAME" 
$ sbt testOnly WriteToGCS
```


## Resources
- [Cloud Storage Client Libraries](https://cloud.google.com/storage/docs/reference/libraries)
- [Cloud Storage connector](https://cloud.google.com/dataproc/docs/concepts/connectors/cloud-storage)