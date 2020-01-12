// See https://docusaurus.io/docs/site-config.html for all the possible
// site configuration options.

const repoUrl = "https://github.com/scalameta/munit";
const baseUrl = "/munit/";

const siteConfig = {
  title: "MUnit",
  tagline: "Scala testing library with actionable errors and extensible APIs",
  url: "http://scalameta.org/munit",
  baseUrl: baseUrl,

  // Used for publishing and more
  projectName: "munit",
  organizationName: "scalameta",

  // algolia: {
  //   apiKey: "c865f6d974a3072a35d4b53d48ac2307",
  //   indexName: "metals"
  // },

  gaTrackingId: "UA-140140828-1",

  // For no header links in the top nav bar -> headerLinks: [],
  headerLinks: [
    { doc: "getting-started", label: "Docs" },
    { href: repoUrl, label: "GitHub", external: true }
  ],

  // If you have users set above, you add it here:
  // users,

  /* path to images for header/footer */
  headerIcon: "img/scalameta-logo.png",
  footerIcon: "img/scalameta-logo.png",
  favicon: "img/favicon.ico",

  /* colors for website */
  colors: {
    primaryColor: "#058772",
    secondaryColor: "#045C4D"
  },

  customDocsPath: "website/target/docs",

  stylesheets: [baseUrl + "css/custom.css"],

  // blogSidebarCount: "ALL",

  // This copyright info is used in /core/Footer.js and blog rss/atom feeds.
  copyright: `Copyright © ${new Date().getFullYear()} Scalameta`,

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks
    theme: "github"
  },

  /* On page navigation for the current documentation page */
  onPageNav: "separate",

  /* Open Graph and Twitter card images */
  ogImage: "img/scalameta-logo.png",
  twitterImage: "img/scalameta-logo.png",

  editUrl: `${repoUrl}/edit/master/docs/`,

  repoUrl
};

module.exports = siteConfig;
