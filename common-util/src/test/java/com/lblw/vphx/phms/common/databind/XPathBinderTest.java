package com.lblw.vphx.phms.common.databind;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class XPathBinderTest {

  private static final String XML_STRING =
      "<root>\n"
          + "<books>\n"
          + "    <book category=\"children\">\n"
          + "        <title lang=\"en\">Harry Potter and the Philosopher's Stone</title>\n"
          + "        <title lang=\"fr\">Harry Potter a l'ecole des sorciers</title>\n"
          + "        <author>J.K. Rowling</author>\n"
          + "        <year>2005</year>\n"
          + "        <price>21.99</price>\n"
          + "        <bookmarked>true</bookmarked>\n"
          + "    </book>\n"
          + "    <book category=\"children\">\n"
          + "        <title lang=\"en\">Harry Potter and the Chamber of Secrets</title>\n"
          + "        <title lang=\"fr\">Harry Potter et la Chambre des Secrets</title>\n"
          + "        <author>J.K. Rowling</author>\n"
          + "        <year>2002</year>\n"
          + "        <price>22.99</price>\n"
          + "    </book>\n"
          + "</books>\n"
          + "<categories>"
          + "    <category>children</category>\n"
          + "    <category>horror</category>\n"
          + "</categories>\n"
          + "</root>";

  private static XPathBinder xPathBinder;

  @BeforeAll
  static void beforeAll() throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Element root =
        builder
            .parse(new ByteArrayInputStream(XML_STRING.getBytes(StandardCharsets.UTF_8)))
            .getDocumentElement();
    xPathBinder = new XPathBinder(root);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  static class BookPOJO {
    private TitlePOJO title;
    private String author;
    private Short year;
    private Float price;

    private Boolean bookmarked;

    @XPathTarget
    public void bindTitle(@XPathTarget.Binding() BindOne<TitlePOJO> binding) {
      this.title = binding.apply(TitlePOJO::new);
    }

    @XPathTarget
    public void bindAuthor(@XPathTarget.Binding(xPath = "author/text()") String author) {
      this.author = author;
    }

    @XPathTarget
    public void bindYear(@XPathTarget.Binding(xPath = "year/text()") String year) {
      this.year = Short.valueOf(year);
    }

    @XPathTarget
    public void bindPrice(@XPathTarget.Binding(xPath = "price/text()") String price) {
      this.price = Float.valueOf(price);
    }

    @XPathTarget
    public void bindBookmarked(
        @XPathTarget.Binding(xPath = "bookmarked/text()") String bookmarked) {
      if (StringUtils.isBlank(bookmarked)) {
        return;
      }
      this.bookmarked = Boolean.valueOf(bookmarked);
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  static class TitlePOJO {
    private String english;
    private String french;

    @XPathTarget
    public void bindEnglish(
        @XPathTarget.Binding(xPath = "title[@lang=\"en\"]/text()") String english) {
      this.english = english;
    }

    @XPathTarget
    public void bindFrench(
        @XPathTarget.Binding(xPath = "title[@lang=\"fr\"]/text()") String french) {
      this.french = french;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  static class LibraryPOJO {
    private List<BookPOJO> books;
    private List<BookPOJO> booksAsync;
    private List<String> categories;

    @XPathTarget
    public void bindBooks(
        @XPathTarget.Binding(xPath = "books/book[x]") BindMany<BookPOJO> bindMany) {
      this.books = bindMany.apply(BookPOJO::new).collect(Collectors.toList());
    }

    @XPathTarget
    public Mono<Void> bindBooksAsync(
        @XPathTarget.Binding(xPath = "books/book[x]") BindMany<BookPOJO> bindMany) {
      return Flux.fromStream(bindMany.apply(BookPOJO::new))
          // Any Async operation
          .map(Function.identity())
          .collectList()
          .doOnNext(books -> this.booksAsync = books)
          .then();
    }

    @XPathTarget
    public void bindCategories(
        @XPathTarget.Binding(xPath = "categories/category[x]/text()") List<String> categories) {
      this.categories = categories;
    }
  }

  @Data
  @Builder
  static class ErroneousMethodLibraryPOJO {
    @XPathTarget()
    public void bindCategories(
        @XPathTarget.Binding(xPath = "categories/category[x]/text()") List<String> categories) {
      throw new RuntimeException("Some Runtime Exception");
    }
  }

  @Data
  @Builder
  static class ErroneousXPathLibraryPOJO {
    @XPathTarget()
    public void bindCategories(
        @XPathTarget.Binding(xPath = "categories/category") List<String> categories) {}
  }

  @Data
  @Builder
  static class UnexpectedParameterLibraryPOJO {
    @XPathTarget()
    public void bindCategories(
        @XPathTarget.Binding(xPath = "categories/category") Float categories) {}
  }

  @Nested
  class GivenPojoWithBindings {
    @Test
    void thenBinds() {
      var toBind = new LibraryPOJO();
      xPathBinder.bind(toBind);
      Assertions.assertThat(toBind)
          .usingRecursiveComparison()
          .isEqualTo(
              LibraryPOJO.builder()
                  .books(
                      List.of(
                          BookPOJO.builder()
                              .title(
                                  TitlePOJO.builder()
                                      .english("Harry Potter and the Philosopher's Stone")
                                      .french("Harry Potter a l'ecole des sorciers")
                                      .build())
                              .author("J.K. Rowling")
                              .year((short) 2005)
                              .price(21.99f)
                              .bookmarked(true)
                              .build(),
                          BookPOJO.builder()
                              .title(
                                  TitlePOJO.builder()
                                      .english("Harry Potter and the Chamber of Secrets")
                                      .french("Harry Potter et la Chambre des Secrets")
                                      .build())
                              .author("J.K. Rowling")
                              .year((short) 2002)
                              .price(22.99f)
                              .build()))
                  .booksAsync(
                      List.of(
                          BookPOJO.builder()
                              .title(
                                  TitlePOJO.builder()
                                      .english("Harry Potter and the Philosopher's Stone")
                                      .french("Harry Potter a l'ecole des sorciers")
                                      .build())
                              .author("J.K. Rowling")
                              .year((short) 2005)
                              .price(21.99f)
                              .bookmarked(true)
                              .build(),
                          BookPOJO.builder()
                              .title(
                                  TitlePOJO.builder()
                                      .english("Harry Potter and the Chamber of Secrets")
                                      .french("Harry Potter et la Chambre des Secrets")
                                      .build())
                              .author("J.K. Rowling")
                              .year((short) 2002)
                              .price(22.99f)
                              .build()))
                  .categories(List.of("children", "horror"))
                  .build());
    }
  }

  @Nested
  class GivenPojoWithErroneousXPathBindings {
    @Test
    void thenThrowsXPathBinderException() {
      var toBind = new ErroneousXPathLibraryPOJO();

      Assertions.assertThatExceptionOfType(XPathBinderException.class)
          .isThrownBy(() -> xPathBinder.bind(toBind))
          .withCause(
              new IllegalArgumentException(
                  "Unexpected parameter type 'List' for xPath 'categories/category' on method 'bindCategories' of instance 'ErroneousXPathLibraryPOJO'"));
    }
  }

  @Nested
  class GivenPojoWithErroneousMethodBindings {
    @Test
    void thenThrowsXPathBinderException() {
      var toBind = new ErroneousMethodLibraryPOJO();

      Assertions.assertThatExceptionOfType(XPathBinderException.class)
          .isThrownBy(() -> xPathBinder.bind(toBind))
          .withMessage(
              "Exception binding method 'bindCategories' of instance 'ErroneousMethodLibraryPOJO' with arguments '[ArrayList]'")
          .withCauseInstanceOf(InvocationTargetException.class);
    }
  }

  @Nested
  class GivenPojoWithUnexpectedParameterBindings {
    @Test
    void thenThrowsXPathBinderException() {
      var toBind = new UnexpectedParameterLibraryPOJO();

      Assertions.assertThatExceptionOfType(XPathBinderException.class)
          .isThrownBy(() -> xPathBinder.bind(toBind))
          .withMessage(
              "java.lang.IllegalArgumentException: Unexpected parameter type 'Float' on method 'bindCategories' of instance 'UnexpectedParameterLibraryPOJO'")
          .withCauseExactlyInstanceOf(IllegalArgumentException.class);
    }
  }
}
