/* Copyright (C) 2013 sumito3478 <sumito3478@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.sumito3478
package aprikot.twitter

import org.scribe.builder._
import org.scribe.builder.api._
import org.scribe.model._
import org.scribe.oauth._

trait Endpoint {
  def oauthConfig: OAuthConfig

  lazy val underlined = new ServiceBuilder().
    provider(classOf[TwitterApi]).
    apiKey(oauthConfig.consumer.key).
    apiSecret(oauthConfig.consumer.secret).
    build

  sealed trait Method

  case object GET extends Method

  case object POST extends Method

  def method: Method

  def url: String
}