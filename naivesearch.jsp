<!-- JSP Page for the layout of the search page -->

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Naive Search</title>
<link href="NaiveSearch/bootstrap/css/bootstrap.min.css"
	rel="stylesheet">
</head>
<body>
<form action="naivesearch" method="post" class="well">
<div>
<label>Enter number of results to be returned</label>
<input type="text" name="topk" class="form-control" size="4"  id="topk"></input>
</div>
<label>Enter Search Query</label>
<input type="text" name="search_query" id="search_query"></input>
<select name="rank_type">
  <option value="cumulative">Pagerank+TF-IDF</option>
  <option value="authorityhubs">Authority Hubs</option>
</select>
<input type="submit" name="submit" id="submit1"></input>
</form>
<div id="result">
<pre>
        ${requestScope.utilOutput}
    </pre>
</div>
</body>
</html>