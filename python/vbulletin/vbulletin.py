'''
vBulletin core
'''

import re
import urlparse


class UnhandledURLStyleError(Exception):
	pass

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
	Topic urls of the type: showthread.php
	'''

	if url.find('showthread.php') < 0:
		return None

	# fix poorly resolved url paths:
	if url.find('forumdisplay.php') >= 0:
		matcher = re.search(r'(forumdisplay.php.*)showthread.php', url).group(1)
		url = url.replace(matcher, '')
		
	parsed = urlparse.urlparse(url)

	if parsed.path.find('showthread.php') < 0:
		return

	if post_id_query(parsed):
		return

	result = topic_id_query_links(parsed) or topic_slug_query_links(parsed) or topic_slug_path_links(parsed)
	if result: return result

	raise UnhandledURLStyleError('Unrecognized url format: %s' % url)

def post_id_query(parsed_url):
	return re.search(r'p=\d+', parsed_url.query)

def topic_id_query_links(parsed_url):
	'''
	showthread.php?t=#/tid=#/threadid=#
	'''
	scheme, netloc, path, params, query, fragment = list(parsed_url)

	query_matcher = re.search(r'(t=\d+|tid=\d+|threadid=\d+)', query)

	if query_matcher:
		return urlparse.urlunparse([
			scheme, 
			netloc, 
			path, 
			params,
			query_matcher.group(1),
			fragment
		])

def topic_slug_query_links(parsed_url):
	'''
	showthread.php?topic-slug
	'''
	if not parsed_url.query:
		return

	query_parts = parsed_url.query.split('&')
	check = [query_part.find('=') < 0 for query_part in query_parts]
	if any(check):
		return urlparse.urlunparse([
			parsed_url.scheme, 
			parsed_url.netloc, 
			parsed_url.path, 
			parsed_url.params,
			query_parts[check.index(True)],
			parsed_url.fragment
		])

def topic_slug_path_links(parsed_url):
	'''
	showthread.php/topic-slug/?nonsense
	'''
	matcher = re.search(r'(showthread.php/.*/?).*', parsed_url.path)

	if matcher:
		return urlparse.urlunparse([
			parsed_url.scheme, 
			parsed_url.netloc, 
			matcher.group(1), 
			parsed_url.params,
			'',
			parsed_url.fragment
		])
