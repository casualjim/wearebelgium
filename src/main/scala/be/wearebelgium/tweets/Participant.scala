package be.wearebelgium.tweets

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.dao._

case class Participant(twitterId: Long, screenName: String, name: String, bio: String, imageUrl: String, accessToken: String, accessSecret: String, @Key("_id") id: ObjectId = new ObjectId)

class ParticipantDao(collection: MongoCollection) extends SalatDAO[Participant, ObjectId](collection) {
  collection.ensureIndex(Map("twitterId" -> 1), "twitter_id_idx", true)
  collection.ensureIndex(Map("screenName" -> 1), "screen_name_idx", true)
  collection.ensureIndex(Map("accessToken" -> 1), "access_token_idx")
  collection.ensureIndex(Map("accessToken" -> 1, "accessSecret" -> 1), "access_token_secret_idx")
}