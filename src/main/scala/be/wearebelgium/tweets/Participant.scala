package be.wearebelgium.tweets

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.dao._

case class Participant(twitterId: Long, screenName: String, name: String, bio: String, imageUrl: String, accessToken: String, accessSecret: String, @Key("_id") id: ObjectId = new ObjectId) {
  def token = OAuthToken(accessToken, accessSecret)
}

class ParticipantDao(collection: MongoCollection) extends SalatDAO[Participant, ObjectId](collection) {
  collection.ensureIndex(Map("twitterId" -> 1), "participant_twitter_id_idx", unique = true)
  collection.ensureIndex(Map("screenName" -> 1), "participant_screen_name_idx", unique = true)
  collection.ensureIndex(Map("accessToken" -> 1), "participant_access_token_idx")
  collection.ensureIndex(Map("accessToken" -> 1, "accessSecret" -> 1), "participant_access_secret_idx")

}