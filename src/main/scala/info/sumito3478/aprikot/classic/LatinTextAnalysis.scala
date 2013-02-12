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

package info.sumito3478.aprikot.classic

import scala.slick.driver.BasicDriver.simple._
import Database.threadLocalSession
import info.sumito3478.aprikot.text._
import info.sumito3478.aprikot.classic.perseus.db.PerseusAnalysisDatum
import info.sumito3478.aprikot.classic.perseus.db.LewisShortDictionaryDatum
import info.sumito3478.aprikot.classic.perseus.AnalysisData
import scala.collection.mutable.ListBuffer
import info.sumito3478.aprikot.classic.perseus.InflectedWord
import info.sumito3478.aprikot.classic.perseus.LemmaDescription
import info.sumito3478.aprikot.classic.perseus.ShortVocabDescription
import info.sumito3478.aprikot.classic.perseus.InflectionDescription

import scala.xml._

class LatinTextAnalysis(
  val inflectionAnalyses: List[AnalysisData],
  val dictionaryHtmls: List[String]) {
  def toHtml: Elem = {
    val analyses = (for (analysis <- inflectionAnalyses) yield <li>{ analysis.inflected }: { analysis.lemma } { analysis.vocab } { analysis.inflection }  </li>).flatten
    val dics = (for (html <- dictionaryHtmls) yield XML.load(Source.fromString(html)).flatten)
    <div class="latin-word-analyses"><div class="latin-inflection-analyses">{ analyses }</div><div class="latin-dictionary-entries">{ dics }</div></div>
  }
}

object LatinTextAnalysis {
  private[this] def openPerseusDatabase = {
    Database.forURL("jdbc:h2:~/.aprikot/perseus_data/perseus_data")
  }

  def apply(text: String): List[LatinTextAnalysis] = {
    val words = text.neutralWordIterator
    val db = openPerseusDatabase
    db withSession {
      val inflectionQuery = for (
        word <- Parameters[String];
        data <- PerseusAnalysisDatum if data.key === word
      ) yield (
        (data.inflected, data.lemma, data.vocab, data.inflection))
      val dicQuery = for (
        key <- Parameters[String];
        data <- LewisShortDictionaryDatum if data.key === key
      ) yield (data.html)
      (for (word <- words) yield {
        val inflectionBuffer = new ListBuffer[AnalysisData]
        val dictionaryBuffer = new ListBuffer[String]
        inflectionQuery(word.toLowerCase) foreach {
          data =>
            val (inflected, lemma, vocab, inflection) = data
            inflectionBuffer += new AnalysisData(new InflectedWord(inflected), new LemmaDescription(lemma), new ShortVocabDescription(vocab), new InflectionDescription(inflection))
            val key = lemma.split(",").toVector.last
            dicQuery(key.toLowerCase) foreach {
              html =>
                dictionaryBuffer += html
            }
        }
        new LatinTextAnalysis(inflectionBuffer.toList, dictionaryBuffer.toList)
      }).toList
    }
  }
}