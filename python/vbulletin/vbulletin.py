'''
vBulletin core
'''

import re
import urlparse


def process_unfriendly_subforum_urls(url):
	'''
	The vBulletin subforums are of the type:
	board.com/optional/forumdisplay.php?topic_name
	OR
	board.com/optional/forumdisplay.php?f=a
	Args:
		- url :  a url containing the forumdisplay key
	Returns:
		- url with only the subforum link
	'''

	scheme, netloc, path, params, query, fragment = list(urlparse.urlparse(url))

	query_parts = query.split('&')

	def not_query_type(x):
		return x.find('=') < 0

	if any([not_query_type(x) for x in query_parts]):
		forum_name_pos = [not_query_type(x) for x in query_parts].index(True)
		query = query_parts[forum_name_pos]

	else:
		query = re.search('.*(f=\d+).*', query).group(1)
		
	return urlparse.urlunparse([scheme, netloc, path, params, query, fragment])

def process_friendly_subforum_urls_1(url):
	'''
	The vBulletin subforums are of the type:
	board.com/f14/
	Args:
		- url :  a url containing the forum path style
	Returns:
		- url with only the subforum link
	'''

	return re.search(r'(.*/f\d+).*', url).group(1)

def attach_slash_end(url):
	return url + '/'

def vbseo_subforum_urls_1(url):
	'''
	The vBulletin subforums are of the type:
	board.com/stuff-f10.html
	OR
	board.com/f12.html
	Args:
		- url :  a url containing the forum path style
	Returns:
		- url with only the subforum link
	'''
	scheme, netloc, path, params, query, fragment = list(urlparse.urlparse(url))
	query = ''
	return urlparse.urlunparse([scheme, netloc, path, params, query, fragment])

def vbseo_subforum_urls_2(url):
	'''
	The vBulletin subforums are of the type:
	board.com/index-page/post_id-post-slug.html
	Args:
		- url :  a url containing the forum path style
	Returns:
		- url with only the subforum link
	'''

	return re.search(r'(.*/)\d+(-.*)*.html', url).group(1)
	
def vbulletin_unfriendly_topics(url):
	'''
	The vBulletin subforums are of the type:
	board.com/index-page/post_id-post-slug.html
	Args:
		- url :  a url containing the forum path style
	Returns:
		- url with only the subforum link
	'''

	def is_topic_id_in_query(query_str):
		return any([query_portion.find('=') < 0 for query_portion in query_str.split('&')])

	def resolve_fucked_up(url_str):
		return url_str.find('forumdisplay') >= 0 and url_str.find('showthread') >= 0

	def query_as_path(url_str):
		return url_str.find('/showthread.php/') >= 0

	# identify which type the url is:
	scheme, netloc, path, params, query, fragment = list(urlparse.urlparse(url))

	query_group = None
	try:
		query_group = re.search(r'(t=\d+|postid=\d+)(.*=.*)*', query).group(1)
	except AttributeError:

		# check if just got a pointer to a post-id.
		# this is useless for us
		if re.search(r'(p=.*)', query):
			return
		
	if query_group:
		query = query_group
		return urlparse.urlunparse([scheme, netloc, path, params, query, fragment])

	elif query and is_topic_id_in_query(query):
		topic_id = query.split('&')[[query_portion.find('=') < 0 for query_portion in query.split('&')].index(True)]
		query = topic_id
		if query.find('/page') >= 0:
			query = re.sub(r'/page\d+', '', query)
		return urlparse.urlunparse([scheme, netloc, path, params, query, fragment])

	elif resolve_fucked_up(url):
		return fix_poor_resolve(url)

	elif query_as_path(url):
		return url

	return url

def fix_poor_resolve(url_str):
	'''
	Java's URI library made a horlicks of resolving some URIs.
	We handle the case where we see URIs of the form:
	forumdisplay.php/foo/showthread.php/bar
	'''
	fuck_up_match = re.search(r'.*(/forumdisplay.*)/showthread.*', url_str).group(1)
	return url_str.replace(fuck_up_match, '')