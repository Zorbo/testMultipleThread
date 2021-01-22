package utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.sky.newcrm.directives.utils.BaseConfiguration;

public class Configuration extends BaseConfiguration {
	private static de.sky.newcrm.http.utils.Configuration configuration = new de.sky.newcrm.http.utils.Configuration();

	public static de.sky.newcrm.http.utils.Configuration getInstance() {
		return configuration;
	}

	private Configuration() {
		super(ConfigFactory.load());
	}

	public int getGetProfileActorReplicas() {
		return getSystemConfig().getInt("get_profile_actor_replicas");
	}

	public int getKafkaActorReplicas() {
		return getSystemConfig().getInt("kafka_actor_replicas");
	}

	public int getManagePersonalDataActorReplicas() {
		return getSystemConfig().getInt("manage_personal_data_actor");
	}

	public String getKafkaAuditTopic() {
		return getSystemConfig().getString("kafka_audit_topic");
	}

	public String getTopicNameForManagePersonalDataConsumer() {
		return getSystemConfig().getString("consumer_manage_personal_data_topic_name");
	}

	public String getTopicNameForCustomerDataProducer() {
		return getSystemConfig().getString("customer_data_change_personal_data_topic");
	}

	private Config getTokenManager() {
		return getSystemConfig().getConfig("token_manager");
	}

	public String getHostTokenMnager() {
		return getTokenManager().getString("host");
	}

	public String getPortTokenMnager() {
		return getTokenManager().getString("port");
	}

	public String getUriGenerateTokenMnager() {
		return getTokenManager().getString("uri_generate_token");
	}

	public String getUriDeleteTokenMnager() {
		return getTokenManager().getString("uri_delete_token");
	}
	
	//CheckPin client
	public Config getCheckPinClientConf() {
		return conf.getConfig("checkpin_client");
	}
	public String getProtocolCheckPinClient() {
		return getCheckPinClientConf().getString("protocol");
	}
	public String getHostCheckPinClient() {
		return getCheckPinClientConf().getString("host");
	}
	public String getPortCheckPinClient() {
		return getCheckPinClientConf().getString("port");
	}
	public String getUriCheckPinClient() {
		return getCheckPinClientConf().getString("uri");
	}
	
	// Security
	public Config getSecurityConf() {
		return conf.getConfig("security");
	}

	public Integer getSaltLength() {
		return getSecurityConf().getInt("salt-length");
	}

	public String getSecureRandomAlgorithm() {
		return getSecurityConf().getString("secure-random-alg");
	}

	public String getSecureHashAlgorithm() {
		return getSecurityConf().getString("secure-hash-alg");
	}

}
