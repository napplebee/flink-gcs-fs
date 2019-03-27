package binx

// notes:
// Interface for exposing the Google Cloud Storage API behavior in a way more amenable to writing
// * filesystem semantics on top of it, without having to deal with API-specific considerations such
// * as HttpTransports, credentials, network errors, batching, etc.
// com.google.cloud.hadoop.gcsio.GoogleCloudStorage

// Configuration for how components should obtain Credentials
// com.google.cloud.hadoop.util.CredentialConfiguration

// CredentialConfiguration based on configuration objects that implement our Entries interface
// com.google.cloud.hadoop.util.EntriesCredentialConfiguration

// Utility class for working with Hadoop-related classes. This should only be used if Hadoop
// * is on the classpath like getting the configuration (loading core-site.xml, hdfs-site.xml and hdfs-default.xml)
// org.apache.flink.runtime.util.HadoopUtils

// /** Miscellaneous helper methods for getting a {@code Credential} from various sources. */
// com.google.cloud.hadoop.util.CredentialFactory
class CreateBucketTest extends TestSpec {
  it should "create a bucket" in {

  }
}
