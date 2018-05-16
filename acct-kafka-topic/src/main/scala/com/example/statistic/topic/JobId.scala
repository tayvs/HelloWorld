package com.example.statistic.topic

case class JobId(campaignId: String, ip: String, domain: String) {

  lazy val jobId: String = campaignId + "_" + ip.replace('.', '_') + "_" + domain.replace('.', '_')

}

object JobId {
  def apply(jobId: String): JobId = {
    val parts = jobId.split('_')
    JobId(parts(0), parts(1), parts(2))
  }
}
