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
	